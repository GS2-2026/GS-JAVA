import java.nio.file.*;
import java.sql.*;
import java.util.*;
public class RepairReadingIds {
 public static void main(String[] args) throws Exception {
  Properties p=new Properties(); try(var in=Files.newInputStream(Path.of("config/application-local.properties"))){p.load(in);}
  Properties cp=new Properties(); cp.setProperty("user",System.getenv().getOrDefault("DB_USERNAME",p.getProperty("spring.datasource.username"))); cp.setProperty("password",System.getenv().getOrDefault("DB_PASSWORD",p.getProperty("spring.datasource.password"))); cp.setProperty("oracle.net.CONNECT_TIMEOUT","10000"); cp.setProperty("oracle.jdbc.ReadTimeout","20000");
  try(Connection c=DriverManager.getConnection(System.getenv().getOrDefault("DB_URL","jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"),cp); Statement s=c.createStatement()) {
   System.out.println(c.getMetaData().getDatabaseProductVersion());
   for(String sql:List.of("SELECT cd_leitura FROM tb_leitura ORDER BY cd_leitura", "SELECT table_name, constraint_name FROM user_constraints WHERE r_constraint_name = 'PK_LEITURA'", "SELECT sequence_name,last_number FROM user_sequences WHERE sequence_name='SQ_TB_LEITURA'")) try(ResultSet r=s.executeQuery(sql)){while(r.next()){for(int i=1;i<=r.getMetaData().getColumnCount();i++)System.out.print(r.getString(i)+" ");System.out.println();}}
   if(args.length==0)return;
   c.setAutoCommit(false);
   s.execute("LOCK TABLE tb_leitura IN EXCLUSIVE MODE NOWAIT");
   s.execute("LOCK TABLE tb_alerta_leitura IN EXCLUSIVE MODE NOWAIT");
   List<long[]> ids=new ArrayList<>(); long next=21;
   try(ResultSet r=s.executeQuery("SELECT cd_leitura FROM tb_leitura WHERE cd_leitura>20 ORDER BY cd_leitura")){while(r.next())ids.add(new long[]{r.getLong(1),next++});}
   // Refuse unexpected schemas or already migrated data.
   try(ResultSet r=s.executeQuery("SELECT COUNT(*) FROM user_constraints WHERE r_constraint_name='PK_LEITURA' AND table_name<>'TB_ALERTA_LEITURA'")){r.next();if(r.getInt(1)!=0)throw new IllegalStateException("Unexpected references");}
   for(long[] pair:ids)if(pair[0]<1000)throw new IllegalStateException("Already migrated or unexpected IDs");
   StringBuilder backup=new StringBuilder();
   for(String table:List.of("tb_leitura","tb_alerta_leitura")){backup.append(table).append('\n');try(ResultSet r=s.executeQuery("SELECT * FROM "+table)){while(r.next()){for(int i=1;i<=r.getMetaData().getColumnCount();i++)backup.append(r.getString(i)).append('\t');backup.append('\n');}}}
   Path out=Path.of(".tools","reading-ids-backup-"+System.currentTimeMillis()+".tsv");Files.writeString(out,backup.toString());System.out.println("Backup: "+out);
   try {
    for(long[] pair:ids){List<Long> alerts=new ArrayList<>();try(ResultSet r=s.executeQuery("SELECT cd_alerta FROM tb_alerta_leitura WHERE cd_leitura="+pair[0])){while(r.next())alerts.add(r.getLong(1));}
     s.executeUpdate("DELETE FROM tb_alerta_leitura WHERE cd_leitura="+pair[0]);
     int changed=s.executeUpdate("UPDATE tb_leitura SET cd_leitura="+pair[1]+" WHERE cd_leitura="+pair[0]);if(changed!=1)throw new IllegalStateException("Unexpected row count");
     for(long a:alerts)s.executeUpdate("INSERT INTO tb_alerta_leitura(cd_alerta,cd_leitura) VALUES ("+a+","+pair[1]+")");
     System.out.println(pair[0]+" -> "+pair[1]);
    }
    c.commit();
   }catch(Exception e){c.rollback();throw e;}
   // Oracle 18c+: preserves sequence object and its grants.
   s.execute("ALTER SEQUENCE sq_tb_leitura RESTART START WITH "+next);
   System.out.println("Next ID: "+next);
   try(ResultSet r=s.executeQuery("SELECT COUNT(*),MAX(cd_leitura) FROM tb_leitura")){r.next();System.out.println("Verified count="+r.getLong(1)+", max="+r.getLong(2));}
  }
 }
}

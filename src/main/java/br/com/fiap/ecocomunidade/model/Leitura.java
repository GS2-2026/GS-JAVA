package br.com.fiap.ecocomunidade.model;
import java.time.*;
import java.math.BigDecimal;

import jakarta.validation.constraints.*;
public class Leitura {
    
    private Long id;
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    
    private Long idMedidor;
    public Long getIdMedidor() { return idMedidor; }
    public void setIdMedidor(Long value) { this.idMedidor = value; }
    
    private LocalDateTime dataHoraLeitura;
    public LocalDateTime getDataHoraLeitura() { return dataHoraLeitura; }
    public void setDataHoraLeitura(LocalDateTime value) { this.dataHoraLeitura = value; }
    
    private BigDecimal consumoLitros;
    public BigDecimal getConsumoLitros() { return consumoLitros; }
    public void setConsumoLitros(BigDecimal value) { this.consumoLitros = value; }
}

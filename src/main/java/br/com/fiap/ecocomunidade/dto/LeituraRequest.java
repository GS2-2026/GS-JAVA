package br.com.fiap.ecocomunidade.dto;
import java.time.*;
import java.math.BigDecimal;

import jakarta.validation.constraints.*;
public class LeituraRequest {
    @NotNull @Positive @Max(999999)
    private Long idMedidor;
    public Long getIdMedidor() { return idMedidor; }
    public void setIdMedidor(Long value) { this.idMedidor = value; }
    @NotNull @PastOrPresent
    private LocalDateTime dataHoraLeitura;
    public LocalDateTime getDataHoraLeitura() { return dataHoraLeitura; }
    public void setDataHoraLeitura(LocalDateTime value) { this.dataHoraLeitura = value; }
    @NotNull @DecimalMin("0.00") @Digits(integer=6, fraction=2)
    private BigDecimal consumoLitros;
    public BigDecimal getConsumoLitros() { return consumoLitros; }
    public void setConsumoLitros(BigDecimal value) { this.consumoLitros = value; }
}

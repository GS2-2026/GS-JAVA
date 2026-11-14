package br.com.fiap.ecocomunidade.dto;
import java.time.*;
import java.math.BigDecimal;

import jakarta.validation.constraints.*;
public class MedidorRequest {
    @NotNull @Positive @Max(999999)
    private Long idResidencia;
    public Long getIdResidencia() { return idResidencia; }
    public void setIdResidencia(Long value) { this.idResidencia = value; }
    @NotBlank @Size(max=20)
    private String numeroSerie;
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String value) { this.numeroSerie = value; }
    @NotNull @PastOrPresent
    private LocalDate dataInstalacao;
    public LocalDate getDataInstalacao() { return dataInstalacao; }
    public void setDataInstalacao(LocalDate value) { this.dataInstalacao = value; }
    @NotBlank @Pattern(regexp="ATIVO|INATIVO|MANUTENCAO")
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
}

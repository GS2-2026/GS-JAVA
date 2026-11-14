package br.com.fiap.ecocomunidade.dto;
import java.time.*;
import java.math.BigDecimal;

import jakarta.validation.constraints.*;
public class ResidenciaRequest {
    @NotNull @Positive @Max(9999)
    private Long idComunidade;
    public Long getIdComunidade() { return idComunidade; }
    public void setIdComunidade(Long value) { this.idComunidade = value; }
    @NotBlank @Size(max=15)
    private String codigoResidencia;
    public String getCodigoResidencia() { return codigoResidencia; }
    public void setCodigoResidencia(String value) { this.codigoResidencia = value; }
    @NotBlank @Size(max=120)
    private String endereco;
    public String getEndereco() { return endereco; }
    public void setEndereco(String value) { this.endereco = value; }
    @NotNull @Min(1) @Max(99)
    private Integer qtdMoradores;
    public Integer getQtdMoradores() { return qtdMoradores; }
    public void setQtdMoradores(Integer value) { this.qtdMoradores = value; }
}

package br.com.fiap.ecocomunidade.dto;
import java.time.*;
import java.math.BigDecimal;

import jakarta.validation.constraints.*;
public class ResidenciaResponse {
    
    private Long id;
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    
    private Long idComunidade;
    public Long getIdComunidade() { return idComunidade; }
    public void setIdComunidade(Long value) { this.idComunidade = value; }
    
    private String codigoResidencia;
    public String getCodigoResidencia() { return codigoResidencia; }
    public void setCodigoResidencia(String value) { this.codigoResidencia = value; }
    
    private String endereco;
    public String getEndereco() { return endereco; }
    public void setEndereco(String value) { this.endereco = value; }
    
    private Integer qtdMoradores;
    public Integer getQtdMoradores() { return qtdMoradores; }
    public void setQtdMoradores(Integer value) { this.qtdMoradores = value; }
}

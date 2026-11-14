package br.com.fiap.ecocomunidade.dto;
import java.time.*;
import java.math.BigDecimal;

import jakarta.validation.constraints.*;
public class MedidorResponse {
    
    private Long id;
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    
    private Long idResidencia;
    public Long getIdResidencia() { return idResidencia; }
    public void setIdResidencia(Long value) { this.idResidencia = value; }
    
    private String numeroSerie;
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String value) { this.numeroSerie = value; }
    
    private LocalDate dataInstalacao;
    public LocalDate getDataInstalacao() { return dataInstalacao; }
    public void setDataInstalacao(LocalDate value) { this.dataInstalacao = value; }
    
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
}

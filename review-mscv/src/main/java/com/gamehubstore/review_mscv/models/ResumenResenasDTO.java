package com.gamehubstore.review_mscv.models;

public class ResumenResenasDTO {
    private Long productId;
    private Double promedio;
    private Long totalResenas;

    public ResumenResenasDTO() {}

    public ResumenResenasDTO(Long productId, Double promedio, Long totalResenas) {
        this.productId = productId;
        this.promedio = promedio;
        this.totalResenas = totalResenas;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Double getPromedio() { return promedio; }
    public void setPromedio(Double promedio) { this.promedio = promedio; }
    public Long getTotalResenas() { return totalResenas; }
    public void setTotalResenas(Long totalResenas) { this.totalResenas = totalResenas; }
}

package com.gamehubstore.promotion_mscv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="promotions", uniqueConstraints=@UniqueConstraint(columnNames="code"))
public class Promotion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotBlank @Column(nullable=false,unique=true) private String code;
 @NotBlank @Column(nullable=false) private String type;
 @NotNull @DecimalMin("0.01") @Column(nullable=false) private BigDecimal value;
 @NotNull @Column(nullable=false) private LocalDate startDate;
 @NotNull @Column(nullable=false) private LocalDate endDate;
 @NotNull @DecimalMin("0.00") private BigDecimal minimumAmount=BigDecimal.ZERO;
 @PositiveOrZero private Integer maxUses;
 @PositiveOrZero private Integer usedCount=0;
 @Column(nullable=false) private boolean active=true;
 public Long getId(){return id;} public String getCode(){return code;} public void setCode(String v){code=v;}
 public String getType(){return type;} public void setType(String v){type=v;} public BigDecimal getValue(){return value;} public void setValue(BigDecimal v){value=v;}
 public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;} public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;}
 public BigDecimal getMinimumAmount(){return minimumAmount;} public void setMinimumAmount(BigDecimal v){minimumAmount=v;} public Integer getMaxUses(){return maxUses;} public void setMaxUses(Integer v){maxUses=v;}
 public Integer getUsedCount(){return usedCount;} public void setUsedCount(Integer v){usedCount=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}

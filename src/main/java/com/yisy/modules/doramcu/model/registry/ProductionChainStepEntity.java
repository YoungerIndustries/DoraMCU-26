package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for individual steps within a production chain.
 */
@Entity
@Table(name = "production_chain_step", indexes = {
        @Index(name = "idx_pcs_chain", columnList = "chain_id"),
        @Index(name = "idx_pcs_step_order", columnList = "chain_id, step_order")
})
public class ProductionChainStepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "chain_id", nullable = false)
    private long chainId;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "input_type", length = 50)
    private String inputType;

    @Column(name = "input_code", length = 50)
    private String inputCode;

    @Column(name = "input_amount")
    private Integer inputAmount;

    @Column(name = "output_type", length = 50)
    private String outputType;

    @Column(name = "output_code", length = 50)
    private String outputCode;

    @Column(name = "output_amount")
    private Integer outputAmount;

    @Column(name = "processing_time_turns")
    private Integer processingTimeTurns;

    @Column(name = "required_industry", length = 50)
    private String requiredIndustry;

    public ProductionChainStepEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getChainId() {
        return chainId;
    }

    public void setChainId(long chainId) {
        this.chainId = chainId;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getInputCode() {
        return inputCode;
    }

    public void setInputCode(String inputCode) {
        this.inputCode = inputCode;
    }

    public Integer getInputAmount() {
        return inputAmount;
    }

    public void setInputAmount(Integer inputAmount) {
        this.inputAmount = inputAmount;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getOutputCode() {
        return outputCode;
    }

    public void setOutputCode(String outputCode) {
        this.outputCode = outputCode;
    }

    public Integer getOutputAmount() {
        return outputAmount;
    }

    public void setOutputAmount(Integer outputAmount) {
        this.outputAmount = outputAmount;
    }

    public Integer getProcessingTimeTurns() {
        return processingTimeTurns;
    }

    public void setProcessingTimeTurns(Integer processingTimeTurns) {
        this.processingTimeTurns = processingTimeTurns;
    }

    public String getRequiredIndustry() {
        return requiredIndustry;
    }

    public void setRequiredIndustry(String requiredIndustry) {
        this.requiredIndustry = requiredIndustry;
    }
}

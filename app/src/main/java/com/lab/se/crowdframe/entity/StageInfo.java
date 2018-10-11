package com.lab.se.crowdframe.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lwh on 2017/3/28.
 */

public class StageInfo implements Serializable{
    private String stageName;
    private String stageDescription;
    private String workerStrategy;
    private boolean allowMultiWorker;
    private int workerNumber;
    private String aggregateMethod;
    private LocationInformation src;
    private LocationInformation dest;

    public StageInfo(){

    }

    public StageInfo(String stageName, String stageDescription, String workerStrategy, boolean allowMultiWorker, int workerNumber, String aggregateMethod, LocationInformation src, LocationInformation dest) {
        this.stageName = stageName;
        this.stageDescription = stageDescription;
        this.workerStrategy = workerStrategy;
        this.allowMultiWorker = allowMultiWorker;
        this.workerNumber = workerNumber;
        this.aggregateMethod = aggregateMethod;
        this.src = src;
        this.dest = dest;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStageDescription() {
        return stageDescription;
    }

    public void setStageDescription(String stageDescription) {
        this.stageDescription = stageDescription;
    }

    public String getWorkerStrategy() {
        return workerStrategy;
    }

    public void setWorkerStrategy(String workerStrategy) {
        this.workerStrategy = workerStrategy;
    }

    public boolean isAllowMultiWorker() {
        return allowMultiWorker;
    }

    public void setAllowMultiWorker(boolean allowMultiWorker) {
        this.allowMultiWorker = allowMultiWorker;
    }

    public int getWorkerNumber() {
        return workerNumber;
    }

    public void setWorkerNumber(int workerNumber) {
        this.workerNumber = workerNumber;
    }

    public String getAggregateMethod() {
        return aggregateMethod;
    }

    public void setAggregateMethod(String aggregateMethod) {
        this.aggregateMethod = aggregateMethod;
    }

    public LocationInformation getSrc() {
        return src;
    }

    public void setSrc(LocationInformation src) {
        this.src = src;
    }

    public LocationInformation getDest() {
        return dest;
    }

    public void setDest(LocationInformation dest) {
        this.dest = dest;
    }

    public static class LocationInformation implements Serializable{
        private String locationStrategy;
        private List<String> inputs;
        private PictureOutput pictureOutput;
        private List<TextOutput> textOutputs;
        private List<NumericalOutput> numericalOutputs;
        private List<EnumOutput> enumOutputs;

        public String getLocationStrategy() {
            return locationStrategy;
        }

        public void setLocationStrategy(String locationStrategy) {
            this.locationStrategy = locationStrategy;
        }

        public List<String> getInputs() {
            return inputs;
        }

        public void setInputs(List<String> inputs) {
            this.inputs = inputs;
        }

        public PictureOutput getPictureOutput() {
            return pictureOutput;
        }

        public void setPictureOutput(PictureOutput pictureOutput) {
            this.pictureOutput = pictureOutput;
        }

        public List<TextOutput> getTextOutputs() {
            return textOutputs;
        }

        public void setTextOutputs(List<TextOutput> textOutputs) {
            this.textOutputs = textOutputs;
        }

        public List<NumericalOutput> getNumericalOutputs() {
            return numericalOutputs;
        }

        public void setNumericalOutputs(List<NumericalOutput> numericalOutputs) {
            this.numericalOutputs = numericalOutputs;
        }

        public List<EnumOutput> getEnumOutputs() {
            return enumOutputs;
        }

        public void setEnumOutputs(List<EnumOutput> enumOutputs) {
            this.enumOutputs = enumOutputs;
        }
    }
}




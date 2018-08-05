package ir.sahab.nimbo.jimbo.kafaconfig;

public enum KafkaTopics {
    URL_FRONTIER("testTopic");

    private String topicName;

    KafkaTopics(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public String toString() {
        return topicName;
    }
}


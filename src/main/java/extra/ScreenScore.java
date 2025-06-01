/**
 * Copyright (C) University of Southampton - All Rights Reserved
 *
 * This source code is protected under international copyright law. All rights
 * reserved and protected by the copyright holders.
 * This file is confidential and only available to authorized individuals with the
 * permission of the copyright holders. If you encounter this file and do not have
 * permission, please contact the copyright holders and delete this file.
 */
package extra;

public class ScreenScore {
    String type;
    String topic;
    int score;

    public ScreenScore(String type, String topic) {
        this.type = type;
        this.topic = topic;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public String getTopic() {
        return topic;
    }

    public int getScore() {
        return score;
    }
}

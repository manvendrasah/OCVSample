package com.xseed.ocvsample.ocvsample.pojo;

/**
 * Created by Manvendra Sah on 20/04/18.
 */

public class Output {

    private int[] answers;
    private int rollNo;
    private int grade;

    public Output() {
        answers = new int[45];
    }

    public int[] getAnswers() {
        return answers;
    }

    public String getAnswerOutputArray() {
        String temp = "[ ";
        int len = answers.length;
        for (int i = 0; i < len - 1; ++i)
            temp += answers[i] + ", ";
        temp += answers[len - 1] + " ]";
        return temp;
    }

    public void setAnswers(int[] answers) {
        this.answers = answers;
    }

    public int getRollNo() {
        return rollNo;
    }

    public void setRollNo(int rollNo) {
        this.rollNo = rollNo;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "Output > TestId : " + rollNo + ", Grade : " + grade + "\nAnswers : " + getAnswerOutputArray();
    }
}

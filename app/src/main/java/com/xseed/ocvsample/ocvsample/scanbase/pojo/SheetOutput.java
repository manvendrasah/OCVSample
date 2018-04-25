package com.xseed.ocvsample.ocvsample.scanbase.pojo;

/**
 * Created by Manvendra Sah on 25/04/18.
 */

public class SheetOutput {
    private int[] answers;
    private int rollNo;
    private int grade;

    public SheetOutput() {
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
        return "SheetOutput > TestId : " + rollNo + ", Grade : " + grade + "\nAnswers : " + getAnswerOutputArray();
    }
}

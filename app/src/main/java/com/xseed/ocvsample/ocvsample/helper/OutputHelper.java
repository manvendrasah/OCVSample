package com.xseed.ocvsample.ocvsample.helper;

import com.xseed.ocvsample.ocvsample.datasource.BlobDS;
import com.xseed.ocvsample.ocvsample.pojo.Blob;
import com.xseed.ocvsample.ocvsample.pojo.Output;

import java.util.HashMap;

/**
 * Created by Manvendra Sah on 20/04/18.
 */

public class OutputHelper {

    public static Output getOutput(BlobDS blobData) {
        Output output = new Output();
        output.setGrade(getGradeFromBlobData(blobData));
        output.setRollNo(getRollNoFromBlobData(blobData));
        output.setAnswers(getAnswerArrayFromBlobData(blobData));
        return output;
    }

    private static int getGradeFromBlobData(BlobDS blobData) {
        if (blobData.gradeBlobs.isEmpty())
            return 0;
        else
            return blobData.gradeBlobs.get(0).index;
    }

    private static int getRollNoFromBlobData(BlobDS blobData) {
        HashMap<Integer, Blob> idBlobs = blobData.idBlobs;
        int size = idBlobs.size();
        int rollNum = 0;
        if (size == 1) {
            rollNum = idBlobs.values().iterator().next().index;
        } else if (size == 2) {
            if (idBlobs.containsKey(1)) {
                if (idBlobs.containsKey(0)) {
                    // first two digits have been marked
                    int first = idBlobs.get(0).index;
                    int second = idBlobs.get(1).index;
                    rollNum = first * 10 + second;
                } else {
                    // last two digits have been marked
                    int second = idBlobs.get(1).index;
                    int third = idBlobs.get(2).index;
                    rollNum = second * 10 + third;
                }
            } else {
                // 1st and 3rd digits have been marked
                // middle digit hasnt been marked, take it as 0
                int first = idBlobs.get(0).index;
                int second = 0;
                int third = idBlobs.get(2).index;
                rollNum = first * 100 + second * 10 + third;
            }
        } else if (size == 3) {
            int first = idBlobs.get(0).index;
            int second = idBlobs.get(1).index;
            int third = idBlobs.get(2).index;
            rollNum = first * 100 + second * 10 + third;
        }
        return rollNum;
    }

    // [ -1, 1, -1, 1, 1, 1, 2, 2, 2, 3, 3, 3, 0, 0, 0, 0, 3, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 0, 0, 0, 0, -1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 3, 0, 0, 3 ]
    private static int[] getAnswerArrayFromBlobData(BlobDS blobData) {
        int[] answers = new int[45];
        HashMap<Integer, Blob> answerBlobs = blobData.answerBlobs;
        int answerIndex = 0;
        // Fill grade in answer array
        int grade = getGradeFromBlobData(blobData);
        if (grade <= 4) {
            answers[0] = grade - 1;
            answers[1] = -1;
            answers[2] = -1;
        } else if (grade > 4 && grade <= 8) {
            answers[0] = -1;
            answers[1] = grade - 5;
            answers[2] = -1;
        } else {
            answers[0] = -1;
            answers[1] = -1;
            answers[2] = grade - 9;
        }
        answerIndex += 3;

        for (int i = 1; i <= 12; ++i) {
            if (answerBlobs.containsKey(i))
                answers[answerIndex] = answerBlobs.get(i).index;
            else
                answers[answerIndex] = -1;
            answerIndex++;
        }

        answers[answerIndex] = 0;
        answerIndex++;
        answers[answerIndex] = 3;
        answerIndex++;
        answers[answerIndex] = 0;
        answerIndex++;

        for (int i = 13; i <= 24; ++i) {
            if (answerBlobs.containsKey(i))
                answers[answerIndex] = answerBlobs.get(i).index;
            else
                answers[answerIndex] = -1;
            answerIndex++;
        }

        answers[answerIndex] = 0;
        answerIndex++;
        answers[answerIndex] = -1;
        answerIndex++;
        answers[answerIndex] = 1;
        answerIndex++;

        for (int i = 25; i <= 35; ++i) {
            if (answerBlobs.containsKey(i))
                answers[answerIndex] = answerBlobs.get(i).index;
            else
                answers[answerIndex] = -1;
            answerIndex++;
        }

        answers[answerIndex] = 3;
        return answers;
    }
}

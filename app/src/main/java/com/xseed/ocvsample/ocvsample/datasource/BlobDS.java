package com.xseed.ocvsample.ocvsample.datasource;

import com.xseed.ocvsample.ocvsample.pojo.Blob;

import java.util.ArrayList;

/**
 * Created by Manvendra Sah on 15/09/17.
 */

public class BlobDS {

    public ArrayList<Blob> answerBlobs = new ArrayList<>();
    public ArrayList<Blob> idBlobs = new ArrayList<>();
    public ArrayList<Blob> gradeBlobs = new ArrayList<>();

    public void setGradeBlob(Blob blob) {
        gradeBlobs.add(blob);
    }

    public void setIdBlob(Blob blob) {
        idBlobs.add(blob);
    }

    public void setAnswerBlob(Blob blob) {
        answerBlobs.add(blob);
    }
}

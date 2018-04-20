package com.xseed.ocvsample.ocvsample.datasource;

import com.xseed.ocvsample.ocvsample.pojo.Blob;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Manvendra Sah on 15/09/17.
 */

public class BlobDS {

    public HashMap<Integer, Blob> answerBlobs = new HashMap<>();
    public HashMap<Integer, Blob> idBlobs = new HashMap<>();
    public ArrayList<Blob> gradeBlobs = new ArrayList<>();

    public void setGradeBlob(Blob blob) {
        gradeBlobs.add(blob);
    }

    public void setIdBlob(Blob blob) {
        idBlobs.put(blob.superIndex, blob);
    }

    public void setAnswerBlob(int index, Blob blob) {
        answerBlobs.put(index, blob);
    }
}

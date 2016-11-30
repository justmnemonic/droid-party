/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package ru.yandex.droidparty.tensorflow;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class DigitClassifier implements Classifier {

  static {
    System.loadLibrary("tensorflow");
  }

  private String inputName;
  private String outputName;
  private int inputSize;

  private int[] intValues;
  private float[] floatValues;
  private float[] outputs;
  private String[] outputNames;

  private TensorFlowInferenceInterface tf;

  public int initializeTensorFlow(
      AssetManager assetManager,
      String modelFilename,
      int inputSize,
      int outputSize,
      String inputName,
      String outputName) throws IOException {

    this.inputName = inputName;
    this.outputName = outputName;
    this.inputSize = inputSize;
    outputNames = new String[] {outputName};
    intValues = new int[inputSize * inputSize];
    floatValues = new float[inputSize * inputSize];
    outputs = new float[outputSize];

    tf = new TensorFlowInferenceInterface();

    return tf.initializeTensorFlow(assetManager, modelFilename);
  }


  @Override
  public ArrayList<Recognition> recognize(final Bitmap bitmap) {
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    for (int i = 0; i < intValues.length; ++i) {
      floatValues[i] = 0xff - intValues[i] & 0xff;
    }
    final int[] dims = {1, inputSize * inputSize};

    tf.fillNodeFloat(inputName, dims, floatValues);
    tf.runInference(outputNames);
    tf.readNodeFloat(outputName, outputs);

    PriorityQueue<Recognition> pq = new PriorityQueue<>(3,
            new Comparator<Recognition>() {
              @Override
              public int compare(Classifier.Recognition lhs, Classifier.Recognition rhs) {
                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
              }
            });
    for (int i = 0; i < outputs.length; ++i) {
      if (outputs[i] > 0.05) {
        pq.add(new Classifier.Recognition(i,  outputs[i]));
      }
    }
    final ArrayList<Recognition> recognitions = new ArrayList<>();
    int recognitionsSize = Math.min(pq.size(), 3);
    for (int i = 0; i < recognitionsSize; ++i) {
      recognitions.add(pq.poll());
    }

    return recognitions;
  }


  @Override
  public void close() {
    tf.close();
  }
}

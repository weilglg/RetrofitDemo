/**
 * Copyright 2017 区长
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.retrofit.network.body;


import com.retrofit.network.callback.ResultProgressCallback;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;


public class ProgressRequestBody extends RequestBody {
    private final RequestBody mRequestBody;
    private final ResultProgressCallback progressListener;
    private final Object tag;


    public ProgressRequestBody(RequestBody requestBody, ResultProgressCallback progressListener, Object tag) {
        this.mRequestBody = requestBody;
        this.progressListener = progressListener;
        this.tag = tag;
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (progressListener == null) {
            mRequestBody.writeTo(sink);
            return;
        }
        ProgressOutputStream progressOutputStream = new ProgressOutputStream(sink.outputStream(), progressListener, contentLength(), tag);
        BufferedSink progressSink = Okio.buffer(Okio.sink(progressOutputStream));
        mRequestBody.writeTo(progressSink);
        progressSink.flush();
    }

}
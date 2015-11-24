/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.plugin.ingest.transport.simulate;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.plugin.ingest.transport.TransportData;

import java.io.IOException;

public class SimulateProcessorResult implements Writeable<SimulateProcessorResult>, ToXContent {

    private static final SimulateProcessorResult PROTOTYPE = new SimulateProcessorResult(null, (IngestDocument)null);

    private String processorId;
    private TransportData data;
    private Exception failure;

    public SimulateProcessorResult(String processorId, IngestDocument ingestDocument) {
        this.processorId = processorId;
        this.data = new TransportData(ingestDocument);
    }

    private SimulateProcessorResult(String processorId, TransportData data) {
        this.processorId = processorId;
        this.data = data;
    }

    public SimulateProcessorResult(String processorId, Exception failure) {
        this.processorId = processorId;
        this.failure = failure;
    }

    public IngestDocument getData() {
        if (data == null) {
            return null;
        }
        return data.get();
    }

    public String getProcessorId() {
        return processorId;
    }

    public Exception getFailure() {
        return failure;
    }

    public static SimulateProcessorResult readSimulateProcessorResultFrom(StreamInput in) throws IOException {
        return PROTOTYPE.readFrom(in);
    }

    @Override
    public SimulateProcessorResult readFrom(StreamInput in) throws IOException {
        String processorId = in.readString();
        if (in.readBoolean()) {
            Exception exception = in.readThrowable();
            return new SimulateProcessorResult(processorId, exception);
        }
        return new SimulateProcessorResult(processorId, TransportData.readTransportDataFrom(in));
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(processorId);
        if (failure == null) {
            out.writeBoolean(false);
            data.writeTo(out);
        } else {
            out.writeBoolean(true);
            out.writeThrowable(failure);
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(Fields.PROCESSOR_ID, processorId);
        if (failure == null) {
            data.toXContent(builder, params);
        } else {
            ElasticsearchException.renderThrowable(builder, params, failure);
        }
        builder.endObject();
        return builder;
    }

    static final class Fields {
        static final XContentBuilderString PROCESSOR_ID = new XContentBuilderString("processor_id");
    }
}
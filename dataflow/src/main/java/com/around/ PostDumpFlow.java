package com.around;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.bigtable.dataflow.CloudBigtableIO;
import com.google.cloud.bigtable.dataflow.CloudBigtableScanConfiguration;
import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.io.Read;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.values.PCollection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;



public class PostDumpFlow {

     private static final String PROJECT_ID = "calm-photon-225722";
   private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

   public static void main(String[] args) {
       // Start by defining the options for the pipeline.
       PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();

       Pipeline p = Pipeline.create(options);

       CloudBigtableScanConfiguration config = new CloudBigtableScanConfiguration.Builder()
               .withProjectId(PROJECT_ID)
               .withInstanceId("around-post")
               .withTableId("post")
               .build();

       PCollection<Result> btRows = p.apply(Read.from(CloudBigtableIO.read(config)));
       PCollection<TableRow> bqRows = btRows.apply(ParDo.of(new DoFn<Result, TableRow>() {
           @Override
           public void processElement(ProcessContext c) {
               Result result = c.element();
               String postId = new String(result.getRow());
               String user = new String(result.getValue(Bytes.toBytes("post"), Bytes.toBytes("user")), UTF8_CHARSET);
               String message = new String(result.getValue(Bytes.toBytes("post"), Bytes.toBytes("message")), UTF8_CHARSET);
               String lat = new String(result.getValue(Bytes.toBytes("location"), Bytes.toBytes("lat")), UTF8_CHARSET);
               String lon = new String(result.getValue(Bytes.toBytes("location"), Bytes.toBytes("lon")), UTF8_CHARSET);
               TableRow row = new TableRow();//BQ Table row object
               row.set("postId", postId);
               row.set("user", user);
               row.set("message", message); row.set("lat", Double.parseDouble(lat));
               row.set("lon", Double.parseDouble(lon));
               c.output(row);
           }
       }));


       List<TableFieldSchema> fields = new ArrayList<>();
       fields.add(new TableFieldSchema().setName("postId").setType("STRING"));
       fields.add(new TableFieldSchema().setName("user").setType("STRING"));
       fields.add(new TableFieldSchema().setName("message").setType("STRING"));
       fields.add(new TableFieldSchema().setName("lat").setType("FLOAT"));
       fields.add(new TableFieldSchema().setName("lon").setType("FLOAT"));

       TableSchema schema = new TableSchema().setFields(fields);
       bqRows.apply(BigQueryIO.Write
               .named("Write")
               .to(PROJECT_ID + ":" + "post_analysis" + "." + "daily_dump_1")
               .withSchema(schema)
               .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
               .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED));

       p.run();
   }
}

package org.impetus;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.filecache.DistributedCache;

public class RandomDataGenerationDriver {
	static Integer option;

	public static class RandomStackOverflowInputFormat extends
			InputFormat<Text, NullWritable> {

		public static final String NUM_MAP_TASKS = "random.generator.map.tasks";
		public static final String NUM_RECORDS_PER_TASK = "random.generator.num.records.per.map.task";
		public static final String RANDOM_WORD_LIST = "random.generator.random.word.file";

		@Override
		public List<InputSplit> getSplits(JobContext job) throws IOException {

			// Get the number of map tasks configured for
			int numSplits = job.getConfiguration().getInt(NUM_MAP_TASKS, -1);
			if (numSplits <= 0) {
				throw new IOException(NUM_MAP_TASKS + " is not set.");
			}

			// Create a number of input splits equivalent to the number of tasks
			ArrayList<InputSplit> splits = new ArrayList<InputSplit>();
			for (int i = 0; i < numSplits; ++i) {
				splits.add(new FakeInputSplit());
			}

			return splits;
		}

		@Override
		public RecordReader<Text, NullWritable> createRecordReader(
				InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {
			// Create a new RandomStackoverflowRecordReader and initialize it
			RandomStackoverflowRecordReader rr = new RandomStackoverflowRecordReader();
			rr.initialize(split, context);
			return rr;
		}

		public static void setNumMapTasks(Job job, int i) {
			job.getConfiguration().setInt(NUM_MAP_TASKS, i);
		}

		public static void setNumRecordPerTask(Job job, int i) {
			job.getConfiguration().setInt(NUM_RECORDS_PER_TASK, i);
		}

		public static class RandomStackoverflowRecordReader extends
				RecordReader<Text, NullWritable> {

			private int numRecordsToCreate = 0;
			private int createdRecords = 0;
			private Text key = new Text();
			private NullWritable value = NullWritable.get();
			private Random rndm = new Random();
			private ArrayList<String> randomWords = new ArrayList<String>();

			// This object will format the creation date string into a Date
			// object
			private SimpleDateFormat frmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

			@Override
			public void initialize(InputSplit split, TaskAttemptContext context)
					throws IOException, InterruptedException {

				// Get the number of records to create from the configuration
				this.numRecordsToCreate = context.getConfiguration().getInt(
						NUM_RECORDS_PER_TASK, -1);

				if (numRecordsToCreate < 0) {
					throw new InvalidParameterException(NUM_RECORDS_PER_TASK
							+ " is not set.");
				}
					//	DistributedCache.getCacheFiles(context
					//	.getConfiguration());
			
			}

			@Override
			public boolean nextKeyValue() throws IOException,
					InterruptedException {
				String randomRecord;
				
				if (createdRecords < numRecordsToCreate) {
						Integer forthFeild = Math.abs(rndm.nextInt()) % 5000;
					Integer smallFeild = Math.abs(rndm.nextInt()) % 10;
					Integer secondField = Math.abs(rndm.nextInt()) % 130000000;
					Integer thirdField = Math.abs(rndm.nextInt()) % Integer.MAX_VALUE;
					Integer fifthField = Math.abs(rndm.nextInt()) % Integer.MAX_VALUE;
					String creationDate = frmt.format(Math.abs(rndm.nextInt()));
				/*	if (createdRecords % 31 == 0) {*/
						randomRecord = " " + "," + secondField + "," + thirdField + ","+ forthFeild + "," + creationDate + "," + fifthField
								+ ";";
				/*	} else {*/
						/*randomRecord = getRandomText(1) + "," + secondField + ","+ thirdField + "," + forthFeild + "," + creationDate
								+ "," + fifthField + ";";*/
				//	}
					randomRecord = secondField + "," + creationDate + ","+ forthFeild + "," + smallFeild + "," + thirdField+","+ forthFeild
							+ ";";
					
				//	randomRecord=Math.abs(rndm.nextInt()) % 5000+","+Math.abs(rndm.nextInt()) % 10+","+Math.abs(rndm.nextInt()) % 13000+Math.abs(rndm.nextInt()) % 100+","+"Mahesh Nair :P"+","+Math.abs(rndm.nextInt()) % 100+","+Math.abs(rndm.nextInt()) % 1000;
					key.set(randomRecord);
					++createdRecords;
					return true;
				} else {
					return false;
				}
			}

			public static Properties getProperties(InputStream iS)
					throws IOException {
				Properties prop = new Properties();
				prop.load(iS);
				return prop;

			}

			// not using now
			private String getRandomText() {
				StringBuilder bldr = new StringBuilder();
				int numWords = Math.abs(rndm.nextInt()) % 30 + 1;

				for (int i = 0; i < numWords; ++i) {
					bldr.append(randomWords.get(Math.abs(rndm.nextInt())
							% randomWords.size())
							);
				}
				return bldr.toString();
			}

			private String getRandomText(int number) {
				StringBuilder bldr = new StringBuilder();
				int numWords = number;

				for (int i = 0; i < numWords; ++i) {
					bldr.append(randomWords.get(Math.abs(rndm.nextInt())% randomWords.size())
							);
				}
				return bldr.toString();
			}

			@Override
			public Text getCurrentKey() throws IOException,
					InterruptedException {
				return key;
			}

			@Override
			public NullWritable getCurrentValue() throws IOException,
					InterruptedException {
				return value;
			}

			@Override
			public float getProgress() throws IOException, InterruptedException {
				return (float) createdRecords / (float) numRecordsToCreate;
			}

			@Override
			public void close() throws IOException {
				// nothing to do here...
			}
		}

		/**
		 * This class is very empty.
		 */
		public static class FakeInputSplit extends InputSplit implements
				Writable {

			@Override
			public void readFields(DataInput arg0) throws IOException {
			}

			@Override
			public void write(DataOutput arg0) throws IOException {
			}

			@Override
			public long getLength() throws IOException, InterruptedException {
				return 0;
			}

			@Override
			public String[] getLocations() throws IOException,
					InterruptedException {
				return new String[0];
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 3) {
			System.err
					.println("Usage: RandomDataGenerationDriver <num map tasks> <num records per task> <output>");
			System.exit(1);
		}
		int numMapTasks = Integer.parseInt(otherArgs[0]);
		int numRecordsPerTask = Integer.parseInt(otherArgs[1]);
		Path outputDir = new Path(otherArgs[2]);
		Job job = new Job(conf, "RandomDataGenerationDriver");
		job.setJarByClass(RandomDataGenerationDriver.class);
		job.setNumReduceTasks(0);
		job.setInputFormatClass(RandomStackOverflowInputFormat.class);
		RandomStackOverflowInputFormat.setNumMapTasks(job, numMapTasks);
		RandomStackOverflowInputFormat.setNumRecordPerTask(job,numRecordsPerTask);

		TextOutputFormat.setOutputPath(job, outputDir);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		System.exit(job.waitForCompletion(true) ? 0 : 2);
	}
}

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
  
import org.apache.hadoop.mapreduce.Reducer;

public class LeastFive {
    public static class LeastFiveMapper extends Mapper<Object,
                            Text, Text, LongWritable> {
  
        private TreeMap<Long, String> tmap;
    
        @Override
        public void setup(Context context) throws IOException, InterruptedException {
            tmap = new TreeMap<Long, String>();
        }
    
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // input data format => movie_name    
            // no_of_views  (tab seperated)
            // we split the input data
            String[] tokens = value.toString().split("\t");
    
            String movie_name = tokens[0];
            long no_of_views = Long.parseLong(tokens[1]);
    
            // insert data into treeMap,
            // we want top 10  viewed movies
            // so we pass no_of_views as key
            tmap.put(no_of_views, movie_name);
    
            // we remove the first key-value
            // if it's size increases 10
            if (tmap.size() > 5) {
                tmap.remove(tmap.lastKey());
            }
        }
  
        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {
            for (Map.Entry<Long, String> entry : tmap.entrySet()) {
    
                long count = entry.getKey();
                String name = entry.getValue();
    
                context.write(new Text(name), new LongWritable(count));
            }
        }
    }


    public static class LeastFiveReducer extends Reducer<Text, LongWritable, LongWritable, Text> {

        private TreeMap<Long, String> tmap2;

        @Override
        public void setup(Context context) throws IOException, InterruptedException {
            tmap2 = new TreeMap<Long, String>();
        }

        @Override
        public void reduce(Text key, Iterable<LongWritable> values,
        Context context) throws IOException, InterruptedException
        {
            // input data from mapper
            // key                values
            // movie_name         [ count ]
            String name = key.toString();
            long count = 0;

            for (LongWritable val : values) {
                count += val.get();
            }

            // insert data into treeMap,
            // we want top 10 viewed movies
            // so we pass count as key
            tmap2.put(count, name);

            // we remove the first key-value
            // if it's size increases 10
            if (tmap2.size() > 5) {
                tmap2.remove(tmap2.lastKey());
            }
        }

        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {
            for (Map.Entry<Long, String> entry : tmap2.entrySet()) {
                long count = entry.getKey();
                String name = entry.getValue();
                context.write(new LongWritable(count), new Text(name));
            }
        }
    }
    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
  
        if (otherArgs.length < 2) 
        {
            System.err.println("Error: please provide two paths");
            System.exit(2);
        }
  
        Job job = Job.getInstance(conf, "top 10");
        job.setJarByClass(LeastFive.class);
  
        job.setMapperClass(LeastFiveMapper.class);
        job.setReducerClass(LeastFiveReducer.class);
  
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setNumReduceTasks(1);
  
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);
  
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
  
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
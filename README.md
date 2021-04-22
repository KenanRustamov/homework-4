# homework-4

All the files needed for running hadoop in the cluster and the base code for leastfive is in the "JAR" folder.

The assignment did not specify the necessary number of outputs that must be generated.  As such, this program generates the least 5 for all the files together.  After I learned about this new information in class, I had already ran out of google cloud credits.  As such, I will place the command i think will allow my program to produce 5 outputs for all files combined, and I will change the code to make output like you stated in the email (without the count on the left side). Here is the command:  hadoop jar -Dmapreduce.job.maps=1 leastfive.jar

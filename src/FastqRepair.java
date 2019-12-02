import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.Arrays;


public class FastqRepair {
    public static void main(String[] argv) throws IOException {
        balance_read_number(argv[0],argv[1]);
    }

    /**
     * Checks if there is a truncated file, if a read or part of a read is missing
     * Prints out detailed errors or that there is no error
     * @param read -  array of strings, should be size 4, one string for each of the line in a fastq read
     * @param file -  the input file this read is from, simply for reporting purposes
     * */
    public static void find_error_in_read(String[] read,String file){
        String[] error_messages = new String[4];
        error_messages[0] = "Line 1: Read header missing / There is no read";
        error_messages[1] = "Line 2: Sequencing missing";
        error_messages[2] = "Line 3: Quality score header missing";
        error_messages[3] = "Line 4: Quality score missing";
        boolean[] errors = new boolean[]{false,false,false,false};
        boolean error_found = false;
        for(int i =0; i < read.length; i++){
            errors[i] = (read[i] == null);
            if(errors[i]){
                error_found = true;
            }
        }
        if(error_found){
            System.out.print("Error in ");
        }else{
            System.out.print("No error found in ");
        }
        System.out.println(file);
    }

    /**
     *
     * @param file1 - string, filepath of the first paired reads fastq.gz file to be read
     * @param file2 - string, filepath of the second paired reads fastq.gz file to be read
     * */
    public static void balance_read_number(String file1, String file2) throws IOException {
        // define in out buffers
        BufferedReader in1 = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(file1))));
        BufferedReader in2 = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(file2))));

        // define output file names
        String outfile1 = file1.replaceAll("\\..*", "");
        outfile1 += "--FastqRepair.fastq.gz";
        String outfile2 = file2.replaceAll("\\..*", "");
        outfile2 += "--FastqRepair.fastq.gz";
        System.out.println("Output files:");
        System.out.println(outfile1);
        System.out.println(outfile2);

        //define output buffers
        GZIPOutputStream out1 = new GZIPOutputStream(new FileOutputStream(outfile1));
        GZIPOutputStream out2 = new GZIPOutputStream(new FileOutputStream(outfile2));

        String content1;
        String content2;
        int count =0;

        // Two file with unbalance read numbers
        // /Users/michael/Research/BenkeFastQs/QTB03_TTAGGC_L003_R1_001.fastq.gz
        // /Users/michael/Research/BenkeFastQs/QTB03_TTAGGC_L003_R2_001.fastq.gz
        // Do these files not have headers? The script seems to go straight to reads with out meta information

        String[] read1 = new String[]{"-1","-1","-1","-1"};
        String[] read2 = new String[]{"-1","-1","-1","-1"};
        try {
            // while there are all 4 lines in a file
            while ((read1[0] = in1.readLine()) != null && (read2[0] = in2.readLine()) != null &&
                    (read1[1] = in1.readLine()) != null && (read2[1] = in2.readLine()) != null &&
                    (read1[2] = in1.readLine()) != null && (read2[2] = in2.readLine()) != null &&
                    (read1[3] = in1.readLine()) != null && (read2[3] = in2.readLine()) != null) {

                byte[] b = null;
                for(int i =0;i<4;i++){
                    //add new line character to end of each string
                    read1[i] += "\n";
                    read2[i] += "\n";
                    // convert string to byte array
                    b = read1[i].getBytes();
                    // write byte array to .gz file
                    out1.write(b,0,read1[i].length());
                    b = read2[i].getBytes();
                    out2.write(b,0,read2[i].length());
                }
                count++;
                //do some validation that the format is actually correct
                if (count % 100000 == 0) {
                    System.out.println(count);
                }
                read1 = new String[]{"-1","-1","-1","-1"};
                read2 = new String[]{"-1","-1","-1","-1"};
            }
        }catch(Exception e){
            System.out.print("Damn, something went wrong while examining read ");
            System.out.println((count+1));
            find_error_in_read(read1,file1);
            find_error_in_read(read2,file2);
            e.printStackTrace();
        }
        System.out.print("Number of complete reads  ");
        System.out.println(count);

        //close the buffers
        out1.close();
        out2.close();
        in1.close();
        in2.close();
    }
}

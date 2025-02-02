import java.io.*;
import java.util.*;

public class GroupedFileWriter {

    public static void main(String[] args) {
        // Make sure the user provides an input file name
        // if (args.length < 1) {
        //     System.out.println("Usage: java GroupedFileWriter <input-file>");
        //     System.exit(1);
        // }

        // Input and output file paths
        String inputFilePath = "index.m3u";
        String outputFilePath = "output.txt";  // Output will be written to this file

        // Map to store grouped content by group-title
        Map<String, List<String>> groupMap = new LinkedHashMap<>(); // Using LinkedHashMap to preserve order

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            String groupTitle = null;
            String titleLine = null;
          

            // Read the file line by line
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("#EXTM3U")) {
                    // Extract the group-title from the EXTINF line
                    continue;
                }

                // Check if the line starts with "#EXTINF:"
                if (line.startsWith("#EXTINF:")) {
                    // Extract the group-title from the EXTINF line
                    titleLine = line;
                    groupTitle = extractGroupTitle(line);
                }

                // Check if there's a URL line following an EXTINF line
                else if (line.startsWith("http") || line.startsWith("#EXTVLCOPT")) {
                    if (titleLine != null) {
                        // Append both EXTINF and URL to the list of this group
                        groupMap.computeIfAbsent(groupTitle, k -> new ArrayList<>())
                                .add(titleLine);
                        groupMap.computeIfAbsent(groupTitle, k -> new ArrayList<>())
                                .add(line);
                        groupTitle = null; // Reset groupTitle after processing
                        titleLine = null;
                    }
                }
            }

            // Write the grouped content to a single output file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                for (Map.Entry<String, List<String>> entry : groupMap.entrySet()) {
                    // Write group-title header
                    //writer.write("### " + entry.getKey() + " ###");
                    writer.newLine();

                    // Write the corresponding EXTINF and URL lines
                    for (String content : entry.getValue()) {
                        writer.write(content);
                        writer.newLine();
                    }

                    // Add a separator between groups for readability
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error writing to output file: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Error reading the input file: " + e.getMessage());
        }
    }

    /**
     * Extracts the group-title from the EXTINF line.
     * If group-title is missing or empty, return "Others".
     */
    private static String extractGroupTitle(String extinfLine) {
        String groupTitle = null;
        int groupTitleIndex = extinfLine.indexOf("group-title=");
        if (groupTitleIndex != -1) {
            // Extract the group-title value between the quotes
            groupTitle = extinfLine.substring(groupTitleIndex+13, extinfLine.lastIndexOf("\"")); // 12 is the length of "group-title="
            //groupTitle = groupTitle.split("\"")[0]; // Get the value between quotes
        }

        // Return "Others" if the group-title is missing or empty
        if (groupTitle == null || groupTitle.isEmpty()) {
            return "Others";
        }
        return groupTitle;
    }
}

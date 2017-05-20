import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static java.lang.System.exit;

/**
 * Main Program Interface
 */
public class Interface {
    private int functionType;
    private int operationMode;
    private int transmissionSize;
    private String inputText, key, initilizationVector;
    private Scanner console;
    private AES aes;
    private String filename;

    /**
     * Main Runnable method called on program entry
     */
    public void run(String[] input) {
        // Setup scanner
        console = new Scanner(System.in);

        System.out.println("Reading in input file...");

        // Read in the file
        try {
            for (String file : input) {
                // Get content from file
                filename = file;
                //String content = readFile("./out/production/COMP3260_A2" + filename, StandardCharsets.UTF_8);
                String content = readFile("./" + filename, StandardCharsets.UTF_8);

                // Setup regex
                String regex = "[\\r\\n]*(?<TYPE>[\\d])[\\r\\n]+(?<MODE>[\\d])[\\r\\n]+(?<TSIZE>[\\d]+)[\\r\\n]+(?<INPUT>[0-9A-Fa-f\\s]{64,95})[\\r\\n]+(?<KEY>[0-9A-Fa-f\\s]{32,47})[\\r\\n]+(?<IV>[0-9A-Fa-f\\s]{32,47})";

                // Setup Pattern and Matcher
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(content);

                // Pull input params via matcher results
                while(m.find()) {
                    this.functionType = Integer.parseInt(m.group("TYPE"));
                    this.operationMode = Integer.parseInt(m.group("MODE"));
                    this.transmissionSize = Integer.parseInt(m.group("TSIZE"));
                    this.inputText = this.clean(m.group("INPUT"));
                    this.key = this.clean(m.group("KEY"));
                    this.initilizationVector = this.clean(m.group("IV"));
                }

                // Perform some quick validation
                while(this.functionType < 0 || this.functionType > 1) {
                    System.out.println("Invalid input of encryption/decryption, please enter 0 for encryption, 1 for decryption or 99 to exit the program.");
                    this.functionType = console.nextInt();

                    // Exit on 99
                    if (this.functionType == 99)
                        exit(0);
                }
                while(this.operationMode < 0 || this.operationMode > 3) {
                    System.out.println("Invalid mode selection, please enter a value from the list below, or 99 to exit:");
                    System.out.println("0 - ECB");
                    System.out.println("1 - CFB");
                    System.out.println("2 - CBC");
                    System.out.println("3 - OFB");
                    this.operationMode = console.nextInt();

                    // Exit on 99
                    if (this.operationMode == 99)
                        exit(0);
                }
                // Check transmission size if applicable

                // Check IV if applicable
                while (this.operationMode == 1 && this.initilizationVector.equals("0")) {
                    System.out.println("IV is required for CFB mode. Please enter one, or 99 to exit.");
                    this.initilizationVector = console.nextLine();

                    // Exit on 99.
                    if (this.initilizationVector.equals("99"))
                        exit(0);
                }

            }
        } catch (IOException ex) {
            System.out.println(ex.toString());
            exit(0);
        }

        // Now we need to branch based on operation mode
        switch (this.operationMode) {
            case 0:
                // ECB
                this.aes = new ECB();
                break;
            case 1:
                // CFB
                this.aes = new CFB();
                // Parse IV
                this.aes.parseIV(this.initilizationVector);
                break;
            case 2:
                // CBC
                this.aes = new CBC();
                // Parse IV
                this.aes.parseIV(this.initilizationVector);
                break;
            case 3:
                // OFB
                this.aes = new OFB();
                break;
            default:
                break;
        }

        // Expand Key
        this.aes.expandedKey = this.aes.keyExpansion(this.key);

        // Now run encrypt/decrypt
        String result;
        if (this.functionType == 0) {
            System.out.println("Encrypting input...");
            result = this.aes.encrypt(this.inputText);
            System.out.println("Finished encrypting!");
        } else {
            System.out.println("Decrypting input...");
            result = this.aes.decrypt(this.inputText);
            System.out.println("Finished decrypting!");
        }

        // Prompt for output type
        System.out.println("Would you like to output result to a file? [Y/n]");
        String out = console.next();
        if (!out.equals("n")) {
            // Output to input output_ + file
            try {
                // Open new file
                //BufferedWriter output_file = Files.newBufferedWriter(Paths.get("./out/production/COMP3260_A2/output_" + filename));
                BufferedWriter output_file = Files.newBufferedWriter(Paths.get("./" + filename));
                // Write results
                if (this.functionType == 0) {
                    output_file.write("Ciphertext:\n");
                } else {
                    output_file.write("Plaintext:\n");
                }
                output_file.write(result);
                // Close file
                output_file.close();
                System.out.println("Results have been stored in output_" + filename);
            } catch (IOException ex) {
                System.out.println("Unable to generate output file.");
            }
        } else {
            // Output to cli and end
            if (this.functionType == 0) {
                System.out.println("\nCiphertext:");
            } else {
                System.out.println("\nPlaintext:");
            }
            System.out.println(result + "\n");
        }
    }

    /**
     * Private helper function to read in file
     */
    private static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * Helper function to remove all whitespace from input
     */
    private String clean(String input) {
        return input.replaceAll("[\\s\\r\\n]*", "");
    }
}

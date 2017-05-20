import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CBC extends AES {

    @Override
    public String encrypt(String input) {
        // Input may be greater than 16 bytes, so split
        Matcher m = Pattern.compile(".{1,32}").matcher(input);

        // Run for each 16 bytes
        int[][] state = new int[4][4];
        String output = "";
        while (m.find()) {
            String chunk = input.substring(m.start(), m.end());

            // Parse string into 4 x 4 state
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    state[k][j] = Integer.parseInt(chunk.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
                }
            }

            // XOR IV
            state = xorIV(state, this.initializationVector);

            // Add round key - round 0
            state = this.addRoundKey(state, 0);

            // Iterate for 10 rounds
            for (int j = 1; j < 10; j++) {
                state = this.subBytes(state);
                state = this.shiftRows(state);
                state = this.mixColumns(state);
                state = this.addRoundKey(state, j);
            }

            // Final round
            state = this.subBytes(state);
            state = this.shiftRows(state);
            state = this.addRoundKey(state, 10);

            // Pass state into initializationVector for next round
            this.initializationVector = this.deepCopyState(state);

            // Add state to output string
            output += this.toString(state);
        }

        return output;
    }

    @Override
    public String decrypt(String input) {
        // Need a tmp array to keep track of the input state
        int[][] initialInput;

        // Input may be greater than 16 bytes, so split
        Matcher m = Pattern.compile(".{1,32}").matcher(input);

        // Run for each 16 bytes
        int[][] state = new int[4][4];
        String output = "";
        while (m.find()) {
            String chunk = input.substring(m.start(), m.end());

            // Parse string into 4 x 4 state
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    state[k][j] = Integer.parseInt(chunk.substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
                }
            }

            // Copy parsed state to initialInput temp
            initialInput = this.deepCopyState(state);

            // Add round key - round 0
            state = this.addRoundKey(state, 10);

            // Iterate for 10 rounds
            for (int j = 9; j > 0; j--) {
                state = this.invSubBytes(state);
                state = this.invShiftRows(state);
                state = this.addRoundKey(state, j);
                state = this.invMixColumns(state);
            }

            // Final round
            state = this.invSubBytes(state);
            state = this.invShiftRows(state);
            state = this.addRoundKey(state, 0);

            // Finally we need to XOR the result with the IV or previous input block
            state = xorIV(state, this.initializationVector);
            // Copy initial state into initializationVector for next round
            this.initializationVector = this.deepCopyState(initialInput);

            output += this.toString(state);
        }

        return output;
    }

    /**
     * Helper function to XOR state with IV
     */
    private int[][] xorIV(int[][] state, int[][] iv) {
        int[][] tmp = new int[4][4];

        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                tmp[j][k] = state[j][k] ^ iv[j][k];
            }
        }

        return tmp;
    }
}

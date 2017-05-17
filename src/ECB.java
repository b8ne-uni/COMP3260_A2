public class ECB extends AES {

    @Override
    public String encrypt(String input) {
        // Input may be greater than 16 bytes, so split
        String[] values = input.split("[A-F0-9]{32}");

        // Run for each 16 bytes
        int[][] state = new int[4][4];
        int recursions = 0;
        String output = "";
        while (recursions < values.length) {
            // Parse string into 4 x 4 state
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    state[j][k] = Integer.parseInt(values[recursions].substring((8 * j) + (2 * k), (8 * j) + (2 * k + 2)), 16);
                }
            }

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

            output = output.concat(this.toString(state));

            recursions++;
        }

        return "";
    }

    @Override
    public String decrypt(String input) {

        return "";
    }
}

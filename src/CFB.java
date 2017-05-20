public class CFB extends AES {

    @Override
    public String encrypt(String input) {
        int[][] state = new int[4][4];
        String output = "";

        // Parse plaintext into a matrix
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[j][i] = Integer.parseInt(input.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
                System.out.println(Integer.toHexString(state[j][i]));
            }
        }

        // Encrypt IV with expandedKey in round zero.
        state = this.addRoundKey(initializationVector, 0);

        // loop through 10 rounds
        // XOR plaintext with encrypted IV
        // encrypt XOR result with expandedKey
        // repeat
        for (int i = 1; i < 10; i++) {
            state = this.subBytes(state);
            state = this.shiftRows(state);
            state = this.mixColumns(state);
            state = this.addRoundKey(state, i);
        }

        // final round
        state = this.subBytes(state);
        state = this.shiftRows(state);
        state = this.addRoundKey(state, 10);

        output = output.concat(this.toString(state));

        return output;

    }

    @Override
    public String decrypt(String input) {

        return "";
    }
}

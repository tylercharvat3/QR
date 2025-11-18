public class Galois {

    // Addition in GF(256) is XOR
    public static int Add(int a, int b) {
        return a ^ b;
    }

    // Multiplication in GF(256) with modulus 0x11D
    public static int Mul(int a, int b) {
        int p = 0;

        for (int i = 0; i < 8; i++) {
            if ((b & 1) != 0)
                p ^= a;

            boolean carry = (a & 0x80) != 0;
            a = (a << 1) & 0xFF;

            if (carry)
                a ^= 0x1D; // 0x11D with x^8 removed (QR standard)

            b >>= 1;
        }

        return p & 0xFF;
    }

    // Exponentiation (used for precomputed generator polynomial, if needed)
    public static int Pow(int power) {
        int a = 1;
        for (int i = 0; i < power; i++) {
            a = xtime(a);
        }
        return a & 0xFF;
    }

    private static int xtime(int x) {
        boolean carry = (x & 0x80) != 0;
        x = (x << 1) & 0xFF;
        if (carry)
            x ^= 0x1D;
        return x;
    }

    // Polynomial division: returns remainder
    public static int[] DividePolynomials(int[] message, int[] generator) {
        int[] buffer = message.clone();
        int generatorDegree = generator.length - 1;

        for (int i = 0; i < message.length - generatorDegree; i++) {
            int coef = buffer[i];
            if (coef != 0) {
                for (int j = 1; j < generator.length; j++) {
                    buffer[i + j] ^= Mul(coef, generator[j]);
                }
            }
        }

        int[] remainder = new int[generatorDegree];
        System.arraycopy(buffer, buffer.length - generatorDegree, remainder, 0, generatorDegree);
        return remainder;
    }
}

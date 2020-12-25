import java.math.BigInteger;
import java.util.Set;
import java.util.Random;
import java.util.HashSet;
public class Part2 {


    private static BigInteger modPowerTwo(BigInteger N, BigInteger R){
        return         N.and(R.subtract(BigInteger.ONE));

    }
    /*
    Montogomer Multiplication
    INPUT : a , b (in montomery representation)
           N , R , NDash = -N^-1 mod R

   OUTPUT : (a * b) R^-1 mod N


     */
    private static BigInteger montogomeryMultiply (BigInteger a,BigInteger b,BigInteger R,int RBitLength,BigInteger N,BigInteger NDash,boolean test){
        BigInteger t = a.multiply(b);
        BigInteger m =  modPowerTwo( t.multiply(NDash),R);
        t = (t.add(m.multiply(N))).shiftRight(RBitLength-1);
        if(t.compareTo(N) > 0) {
           if(!test)for(int i=0;i<100;i++)t.subtract(N);
            return t.subtract(N);
        }
        else
            return t;
    }


    private static BigInteger decryptMontgomery(BigInteger c ,BigInteger d , BigInteger modulus,int expLen){
        return modExpMontgomery(c,d,modulus,expLen);
    }
    private static BigInteger encryptMontgomery(BigInteger c ,BigInteger d , BigInteger modulus,int expLen){
        return modExpMontgomery(c,d,modulus, expLen);
    }


    private static BigInteger modExpMontgomery(BigInteger a, BigInteger exponent, BigInteger N, int expLen) {

        int len = N.bitLength();
        BigInteger R = BigInteger.ONE.shiftLeft(len);
        int expBitlength = exponent.bitLength();
        BigInteger NDash = N.modInverse(R).negate();


        BigInteger result = a.multiply(R).mod(N);
        BigInteger aa=result;
        for (int i = expBitlength - 2; i >= expBitlength-2-expLen; i--) {
            result = montogomeryMultiply(result,result,R,len+1,N,NDash,false);

            if (exponent.testBit(i)) {
                result =montogomeryMultiply(result,aa,R,len+1,N,NDash,false);
            }

        }


        return  result.multiply(R.modInverse(N)).mod(N);


    }

    private static boolean montogomeryMultiplyTester(BigInteger a, BigInteger b, BigInteger R, int RBitLength, BigInteger N, BigInteger NDash){
        BigInteger t = a.multiply(b);
        BigInteger m =  modPowerTwo( t.multiply(NDash),R);
        t = (t.add(m.multiply(N))).shiftRight(RBitLength-1);
        if(t.compareTo(N) > 0) {

            return true;
        }
        else
            return false;
    }


    public static void main(String[] args) {

        // RSA modulus
        BigInteger modulus = new BigInteger(
                "a12360b5a6d58b1a7468ce7a7158f7a2562611bd163ae754996bc6a2421aa17d3cf6d4d46a06a9d437525571a2bfe9395d440d7b09e9912a2a1f2e6cb072da2d0534cd626acf8451c0f0f1dca1ac0c18017536ea314cf3d2fa5e27a13000c4542e4cf86b407b2255f9819a763797c221c8ed7e7050bc1c9e57c35d5bb0bddcdb98f4a1b58f6d8b8d6edb292fd0f7fa82dc5fdcd78b04ca09e7bc3f4164d901b119c4f427d054e7848fdf7110352c4e612d02489da801ec9ab978d98831fa7f872fa750b092967ff6bdd223199af209383bbce36799a5ed5856f587f7d420e8d76a58b398ef1f7b290bc5b75ef59182bfa02fafb7caeb504bd9f77348aea61ae9",
                16);

        // private exponent
        BigInteger d = new BigInteger(
                "1801d152befc69b1134eda145bf6c94e224fa1acee36f06826436c609840a776a532911ae48101a460699fd9424a1d51329804fa23cbec98bf95cdb0dbc900c05c5a358f48228ab03372b25610b0354d0e4a8c57efe86b1b2fb9ff6580655cdabddb31d7a8cfaf99e7866ba0d93f7ee8d1aab07fc347836c03df537569ab9fcfca8ebf5662feafbdf196bb6c925dbc878f89985096fabd6430511c0ca9c4d99b6f9f5dd9aa3ddfac12f6c2d3194ab99c897ba25bf71e53cd33c1573e242d75c48cd2537d1766bbbf4f7235c40ce3f49b18e00c874932412743dc28b7d3d32e85c922c1d9a8e5bf4c7dd6fe4545dd699295d51945d1fc507c24a709e87561b001",
                16);

        int nSamples = 10000;
        int trials = 20;
        int[] explen = {3, 5, 10, 20, 50, 100};

        for (int exLen : explen) {
            int[][] TrialOneZero = new int[trials][2];
           System.out.println("For exp bit len = "+exLen);

            for (int j = 0; j < trials; j++) {

                BigInteger[] mArr = new BigInteger[nSamples];
                long[] tArr = new long[nSamples];
                for (int i = 0; i < nSamples; i++) {

                    Random rnd = new Random();
                    BigInteger m = new BigInteger(modulus.bitLength() - 1, rnd);
                    long start = System.nanoTime();
                    BigInteger m2 = decryptMontgomery(m, d, modulus ,exLen);
                    long elapsedTime = System.nanoTime() - start;
                    mArr[i]=m;
                    tArr[i]=elapsedTime;

                }
                System.out.println("Finished collecting samples for trial = "+(j+1));
                Set<Long> oneTrue = new HashSet<>();
                Set<Long> oneFalse = new HashSet<>();
                Set<Long> zeroTrue = new HashSet<>();
                Set<Long> zeroFalse = new HashSet<>();
                int i=0;
                int len = modulus.bitLength();
                BigInteger R = BigInteger.ONE.shiftLeft(len);
                BigInteger NDash = modulus.modInverse(R).negate();

                for (BigInteger a : mArr) {
                    long time = tArr[i++];
                    BigInteger aa = a.multiply(R).mod(modulus);
                    BigInteger result = montogomeryMultiply(aa,aa,R,len+1,modulus,NDash,true);
                    BigInteger r2 = montogomeryMultiply(result,aa,R,len+1,modulus,NDash,true);

                    if(montogomeryMultiplyTester(r2,r2,R,len+1,modulus,NDash))
                        zeroTrue.add(time);
                    else
                        zeroFalse.add(time);
                    if(montogomeryMultiplyTester(result,result,R,len+1,modulus,NDash))
                        oneTrue.add(time);
                    else
                        oneFalse.add(time);

                }
                long m1 = getAverage(oneTrue);
                long m2 = getAverage(oneFalse);
                long oneAvgDifference=Math.abs(m1 - m2);
                long m3 = getAverage(zeroTrue);
                long m4 = getAverage(zeroFalse);
                long zeroAvgDifference=Math.abs(m3 - m4);
                System.out.println("One Average Difference  = "+oneAvgDifference+" At Trial "+(j+1));
                System.out.println("Zero Average Difference = "+zeroAvgDifference+" At Trial "+(j+1));
                addResult(TrialOneZero,oneAvgDifference,zeroAvgDifference,j);

            }
        }
    }
    private static long getAverage(Set<Long> set){
        int size = set.size();
        long avg =0;
        for(long time : set){
            avg += (time/size);
        }
        return avg;
    }
    private static void addResult(int[][] arr,long oneDiff,long zeroDiff,int stage){
        if(oneDiff>zeroDiff)
            arr[stage][0]++;
        else
            arr[stage][1]++;
        if(stage==arr.length-1)
        {
            int one =0;
            int zero=0;
            for(int i=0;i<arr.length;i++){
                if(arr[i][0]==1)
                    one++;
                else
                    zero++;
            }
            System.out.println("The total one is greater is "+one+" And the total zero is greater is "+zero);
            if(one>zero)
                System.out.println("Assumption the target bit is 1 wins");
            else if(zero>one)
                System.out.println("Assumption the target bit is 0 wins");
            else
                System.out.println("Assumption failed");
        }
    }


}

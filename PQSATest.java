import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class PQSATest {

    static class QuickAction extends RecursiveAction
    {

        private int[] a;
        private int lo;
        private int hi;

        public QuickAction(int[] a, int lo, int hi) {
            this.a = a;
            this.lo = lo;
            this.hi = hi;
        }

        protected void compute() {
            if (hi <= lo) return;
            int j = partition(a, lo, hi);

            //partitions into left, right arrays
            PQSATest.QuickAction quickLeft = new PQSATest.QuickAction(a, lo, j-1);
            PQSATest.QuickAction quickRight = new PQSATest.QuickAction(a, j+1, hi);

            //non-recursively forks each left-right partition
            quickLeft.fork();
            quickRight.fork();

            quickLeft.join();
            quickRight.join();
        }
        //basic quicksort sorting algorithm
        private static int partition(int[] a, int lo, int hi)
        {
            Random rand = new Random();
            int randomIndex = rand.nextInt(hi - lo + 1) + lo;

            //random pivot chosen, swapped to end of array
            int tem = a[randomIndex];
            a[randomIndex] = a[lo];
            a[lo] = tem;
            int i = lo, j = hi+1;
            while (true)
            {
                while (a[++i] < a[lo])
                    if (i == hi) break;
                while (a[lo] < a[--j])
                    if (j == lo) break;
                if (i >= j) break;


                int temp = a[j];
                a[j] = a[i];
                a[i] = temp;
            }

            int temp = a[lo];
            a[lo] = a[j];
            a[j] = temp;

            return j;
        }

    }
    private static void shuffleArray(int[] array)
    {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            if (index != i)
            {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }
    public static long calculateAverage(long[] arr) {
        if (arr.length == 0) {
            return 0; // Handle empty array case
        }

        int sum = 0;
        for (long num : arr) {
            sum += num;
        }

        return sum / arr.length;
    }

    //prints sorted array for testing
    static int[] testValidity() {
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        int[] array = new Random().ints(0, 65536).limit(10000).toArray();
        shuffleArray(array);
        PQSATest.QuickAction action = new PQSATest.QuickAction(array, 0, array.length - 1);
        forkJoinPool.invoke(action);
        return array;
    }
    static long[] testSpeed(int arraySize) {
        long[] averageTimeArray = new long[4];
        long[] testedTimeArray = new long[10];
        int[] parallelArray = {1, 2, 4, 8};

        for (int i = 0; i < 4; i++) {
            ForkJoinPool forkJoinPool = new ForkJoinPool(parallelArray[i]);
            for (int j = 0; j < 10; j++) {
                int[] array = new Random().ints(0, 65536).limit(arraySize).toArray();
                shuffleArray(array);
                PQSATest.QuickAction action = new PQSATest.QuickAction(array, 0, array.length - 1);
                final long startTime = System.currentTimeMillis();
                forkJoinPool.invoke(action);
                final long endTime = System.currentTimeMillis();
                testedTimeArray[j] = endTime - startTime;
            }
            averageTimeArray[i] = calculateAverage(testedTimeArray);

        }
        return averageTimeArray;
    }

    public static void main(String[] args) {

            long[][] totalTimeArray = new long[3][4];
            int[] sizeArray = {1000000, 2000000, 5000000};
            //stores the time taken for each array size, for each processor count
            for (int i=0; i < 3; i++) {
                totalTimeArray[i] = testSpeed(sizeArray[i]);
            }

            for (int i=0; i < 3; i++) {
                System.out.println(Arrays.toString(totalTimeArray[i]));
            }
        //System.out.println(Arrays.toString(testValidity()));
    }
}

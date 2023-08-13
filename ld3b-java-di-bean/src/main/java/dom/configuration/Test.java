package dom.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String args[])
    {
    	
    	 String s1="Samsung",s2="Samsung";
         
         if(s1==s2) {
         	System.out.println("Test 1 ");
         }
         
         if(s1.equals(s2)) {
         	System.out.println("Test 1 ");
         }
    	
        int[] arr = { 1, 5, 7, -1, 5 };
        int sum = 6;
        int count = 0; // Initialize result
        
       
        
        // Consider all possible pairs and check their sums
        for (int i = 0; i < arr.length; i++)
            for (int j = i + 1; j < arr.length; j++)
                if ((arr[i] + arr[j]) == sum)
                    count++;
        System.err.println("---------------------------------------------------------");
        List<Integer> son = Arrays.stream(arr).boxed().collect(Collectors.toList());
        
        
        for(int i = 0 ;i<arr.length;i++) {
        	if(son.contains(sum-arr[i])) {
        		System.err.println(arr[i]+", "+(sum-arr[i]));
        	}
        }
        
//        System.out.printf("Count of pairs is %d", count);
    }
}
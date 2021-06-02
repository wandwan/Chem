import java.util.*;
public class Monomer {
	ArrayList<Temp>[] ans;
	double[] mass;
	public Monomer(int n) {
		ans = new ArrayList[n + 1];
	}
	public Monomer(double n, double[] arr, double precision, double tolerance) {
		this((int) (tolerance * precision));
		solve(n,arr,precision,tolerance);
	}
	public void solve(double n, double[] arr, double precision, double tolerance) {
//		System.out.println(n + " " + precision + " " + tolerance);
//		System.out.println(Arrays.toString(arr));
		n *= precision;
		tolerance *= precision;
		n = Math.round(n);
		tolerance = Math.round(tolerance);
		int[] dp = new int[(int) (n + tolerance) + 1];
		dp[0] = 1;

		for(int j = 0; j < arr.length; j++)
			for(int i = 0; i < dp.length; i++)
				if(dp[i] != 0 && (int) Math.round(arr[j] * precision) + i < dp.length)
					dp[i + (int) Math.round(arr[j] * precision)] += dp[i];
		ans[0] = new ArrayList<Temp>();
		addSol((int) (n),dp,arr,precision,new int[arr.length], 0, ans[0], (int) n);
		for(int i = 1; i <= tolerance; i++) {
			ans[i] = new ArrayList<Temp>();
			if(n - i > -1) {
				addSol((int) (n - i),dp,arr,precision,new int[arr.length], 0, ans[i], (int) n - i);
			}
			if(n + i < dp.length) {
				addSol((int) (n + i),dp,arr,precision,new int[arr.length], 0, ans[i], (int) n - i);
			}
		}
//		System.out.println(ans);
//		System.out.println(Arrays.toString(dp));
	}
	public void addSol(int curr, int[] dp, double[] arr, double precision, int[] tans, int bot, ArrayList<Temp> arr1, int top) {
		if(curr == 0) {
			arr1.add(new Temp(Arrays.copyOf(tans, tans.length + 1)));
			arr1.get(arr1.size() - 1).a[tans.length] = top;
			return;
		}
		for(int i = bot; i < arr.length; i++) {
//			System.out.println(curr);
			if(curr - (int) Math.round(arr[i] * precision) > -1 && dp[curr - (int) Math.round(arr[i] * precision)] > 0) {
				tans[i]++;
				addSol(curr - (int) Math.round(arr[i] * precision), dp, arr, precision, tans, i, arr1, top);
				tans[i]--;
			}
		}
	}
}
class Temp {
	int[] a;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(a);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Temp other = (Temp) obj;
		if (!Arrays.equals(a, other.a))
			return false;
		return true;
	}

	public Temp(int[] a) {
		super();
		this.a = a;
	}

	@Override
	public String toString() {
		return "Temp [a=" + Arrays.toString(a) + "]";
	}
	
}

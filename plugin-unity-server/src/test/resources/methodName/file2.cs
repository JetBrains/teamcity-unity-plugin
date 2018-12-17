public class MyScriptName {

	public static void MyMethod () {
		Debug.Log ("Here you put your code");
	}

	public void NonStaticMethod() {
	    MyPrivateMethod();
	    MyInternalMethod();
	}

	private static void MyPrivateMethod() {
	}

	internal static void MyInternalMethod() {
	}
}
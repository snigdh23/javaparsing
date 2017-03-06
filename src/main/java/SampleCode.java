/**
 * Created by snigdhc on 16/2/17.
 */

public class SampleCode {

    private String name;

    void abcd(){
        System.out.println("ABCD");
    }

    int fact(int n, SampleCode f, String sa){
        f.faa(name);
        sa.toUpperCase();
        sa.toLowerCase();
        if(n<=1){
            return 1;
        }
        else{
            return(n*fact(n-1,f,sa));
        }
    } //fact end

    int add(int a, int b){ return(a+b);}

    String faa(String a){ a.toUpperCase(); a.toLowerCase(); return(a); }

    public static void main(String args[]){

        SampleCode ft = new SampleCode();
        args[0].toLowerCase();
        ft.name = "UN DOS TRES";
        int result = ft.fact(6,ft,"Skyfall");
        System.out.println(result);
        String la = ft.faa("Hola");
        System.out.println(la);
        int rs = ft.add(3,4);
        System.out.println(rs);
    } //main end
} //class end
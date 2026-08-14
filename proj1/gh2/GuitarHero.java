package gh2;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {
    public static final String KEYBOARD = "q2we4r5ty7u8i9op-[=zxcvbnm,./"; //对应键盘上的 37 个字符

    public static void main(String[] args) {
        // 初始化 37 根琴弦数组
        GuitarString[] strings = new GuitarString[KEYBOARD.length()];

        for (int i = 0; i < KEYBOARD.length(); i++) {
            // 根据公式计算第 i 个音的频率：440.0 * 2^((i - 24) / 12.0)
            double frequency = 440.0 * Math.pow(2.0, (i - 24.0) / 12.0);
            strings[i] = new GuitarString(frequency);
        }

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {       // 检查用户是否按键
                char key = StdDraw.nextKeyTyped(); // 读取用户的按键
                int index = KEYBOARD.indexOf(key); // 查看按下的键是否在 37 个键里, 返回对应的位置
                if (index != -1) {                 // 如果找到了 (-1 说明按键不在开头定义好的键盘上)
                    strings[index].pluck();        // 拨动对应位置的那根弦！
                }
            }

            double sample = 0.0;
            for (GuitarString s : strings) {
                sample += s.sample();
            }

            StdAudio.play(sample);

            for (GuitarString s : strings) {
                s.tic();
            }
        }
    }
}

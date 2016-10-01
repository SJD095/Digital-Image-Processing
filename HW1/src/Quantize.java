import java.util.*;
import javax.imageio.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class Quantize
{
	//将原图变为灰度图，即将每个像素的rgba改成心理上的灰度值
	public static BufferedImage translate_to_gray(BufferedImage input)
	{
		//获取图片的分辨率信息
		int height = input.getHeight();
		int widith = input.getWidth();

		//遍历每个像素
		for(int i = 0; i < widith; i++)
		{
			for(int j = 0; j < height; j++)
			{
				//对每个像素，操作其RGB值使之看起来是灰色的
				int rgba = input.getRGB(i, j);
				input.setRGB(i, j, ranger(rgba));
			}
		}
		
		return input;
	}

	public static int ranger(int rgba_input)
	{
		//采用位运算截取出单独的RGBA的值
		int a = (rgba_input >> 24) & 0xFF;
		int r = (rgba_input >> 16) & 0xFF;
		int g = (rgba_input >> 8) & 0xFF;
		int b = rgba_input & 0xFF;

		//心理上采用此比例 0.299 0.587 0.114 分别乘 RGB 可使得图片变成灰色
		int gray = (int) (r * 0.299 + g * 0.587 + b * 0.114);

		//通过移位操作构造新的RGBA值
		int rgba_output = ((a << 24) & 0xFF000000) | ((gray << 16) & 0x00FF0000) | ((gray << 8) & 0x0000FF00) | (gray & 0x000000FF);

		return rgba_output;
	}

	//根据输入的灰度图像和要转变的灰度值生成新的图像
	public static BufferedImage gray_to_new(BufferedImage gray_img, int level)
	{
		//计算出新的灰度间距
		int gap = (int) (256 / (level - 1));
		int width = gray_img.getWidth();
		int height = gray_img.getHeight();

		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				//根据新灰度改变每个像素的灰度值
				int rgba = gray_img.getRGB(i, j);
				gray_img.setRGB(i, j, new_rgba(rgba, gap));
			}
		}

		return gray_img;
	}
	
	//计算新的灰度值
	public static int new_rgba(int rgba, int gap)
	{
		//透明度保持不变
		int a = (rgba >> 24) & 0xFF;
		int new_rgb = rgba & 0xFF;

		//如果值在灰度间距的一半以上，则向上取值，否则向下取值
		double position = ((double)new_rgb - (int)(new_rgb / gap) * gap) / gap;
		if (position > 0.4) {
			new_rgb = (new_rgb / gap + 1) * gap;
			if (new_rgb > 255) new_rgb = 255;
		} else {
			new_rgb = (new_rgb / gap) * gap;
		}

		//构造新的RGBA值
		int rgba_output = ((a << 24) & 0xFF000000) | ((new_rgb << 16) & 0x00FF0000) | ((new_rgb << 8) & 0x0000FF00) | (new_rgb & 0x000000FF);

		return rgba_output;
	}

	public static void main(String[] args)
	{
		Scanner in = new Scanner(System.in);
		String filename = in.nextLine();
		int level = in.nextInt();

		File f = new File(filename);
		BufferedImage tmp;
		BufferedImage input;
		
		try {
			input = ImageIO.read(f);
			tmp = translate_to_gray(input);
			BufferedImage output = gray_to_new(tmp, level);
			
			//输出图像名为output.png
			File newFile = new File("output.png");
			ImageIO.write(output, "png", newFile);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		in.close();
	}
}

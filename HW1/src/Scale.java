import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.File;
import java.io.IOException;

public class Scale
{
    //将获取到的图片转化为一维数组
    public static int[] get_one_array(BufferedImage img)
    {
        int width = img.getWidth();
        int height = img.getHeight();

        int[] result = new int[width * height];

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                result[i + j * width] = img.getRGB(i, j);
            }
        }

        return result;
    }

    //采用双线性内插法得到新的图像的一维数组
    public static int[] compute_result(int[] all_pixels, int from_w, int from_h, int to_w, int to_h)
    {
        //将原图的一维数组转为三维数组，方便根据坐标处理RGBA值
        int[][][] pixels_for_deal = to_three(all_pixels, from_w, from_h);

        //存储输出图像的三维数组
        int[][][] tmp_for_deal = new int[to_h][to_w][4];

        //存储输出图像的一维数组
        int[] output;

        //求出输出图像相对原图像的比率
        double rate_width = (double) from_w / (double) to_w;
        double rate_height = (double) from_h / (double) to_h;

        //对新图像的每一行和每一列，根据旧图像进行线性差值
        for(int row = 0; row < to_h; row++)
        {
            //求出新图像相对旧图像对应的列
            double srcRow = ((float)row) * rate_height;
            double j = Math.floor(srcRow);

            //存储列的小数部分
            double t = srcRow - j;

            for(int col = 0; col < to_w; col++)
            {
                //求出新图像相对旧图像对应的行
                double srcCol = ((float)col) * rate_width;
                double k = Math.floor(srcCol);

                //存储行的小数部分
                double u = srcCol - k;

                //求出四个像素对应的比例
                double pos1 = (1.0d - t) * (1.0d - u);
                double pos2 = (t) * (1.0d - u);
                double pos3 = (1.0d - t) * u;
                double pos4 = t * u;

                //根据比例加权平均四个像素，并存储进新图像的对应位置
                for (int i = 0; i < 4; i++)
                {
                    tmp_for_deal[row][col][i] = (int)(pos1 * pixels_for_deal[safe_range((int)j, from_h - 1, 0)][safe_range((int)k, from_w - 1, 0)][i] + pos2 * pixels_for_deal[safe_range((int)(j + 1), from_h - 1, 0)][safe_range((int)k, from_w - 1, 0)][i] + pos3 * pixels_for_deal[safe_range((int)j, from_h - 1, 0)][safe_range((int)(k + 1), from_w - 1, 0)][i] + pos4 * pixels_for_deal[safe_range((int)(j + 1), from_h - 1, 0)][safe_range((int)(k + 1),from_w - 1, 0)][i]);
                }
            }
        }

        //返回一个一维数组
        output = to_one(tmp_for_deal, to_w, to_h);
        return output;
    }

    //将三维数组转换为一维数组
    public static int[] to_one(int[][][] data, int imgCols, int imgRows)
    {
        int[] one_array = new int[imgCols * imgRows];

        for (int row = 0, count = 0; row < imgRows; row++)
        {
            for (int col = 0; col < imgCols; col++)
            {
                //一维数组的每个值对应三维数组的四个值
                one_array[count] = ((data[row][col][0] << 24) & 0xFF000000) | ((data[row][col][1] << 16) & 0x00FF0000) | ((data[row][col][2] << 8) & 0x0000FF00) | ((data[row][col][3]) & 0x000000FF);

                count++;
            }
        }
        return one_array;
    }

    //将一维数组转换为三维数组
    public static int [][][] to_three(int[] one_array, int width, int height)
    {
        //将RGBA分别存储，方便处理
        int[][][] tmp = new int[height][width][4];
        for(int row = 0; row < height; row++)
        {
            //存储一整行的RGBA值
            int[] aRow = new int[width];
            for (int col = 0; col < width; col++)
            {
                int element = row * width + col;
                aRow[col] = one_array[element];
            }

            //将一维数组中的RGBA分别存储进三维数组对应位置的四个值中
            for(int col = 0; col < width; col++)
            {
                tmp[row][col][0] = (aRow[col] >> 24) & 0xFF; // alpha
                tmp[row][col][1] = (aRow[col] >> 16) & 0xFF; // red
                tmp[row][col][2] = (aRow[col] >> 8) & 0xFF;  // green
                tmp[row][col][3] = (aRow[col]) & 0xFF;       // blue
            }

        }

        return tmp;
    }

    //确保像素位置不越界
    public static int safe_range(int x, int max, int min)
    {
        return x > max ? max : x < min ? min : x;
    }

    //存储图像到本地
    public static void save(int width, int height, int[] input, String fileName) throws IOException
    {
        //这里要存储图像为TYPE_BYTE_GRAY的灰度图格式，不能存为RGB格式
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for(int i = 0 ; i < width; i++)
        {
            for(int j = 0 ; j < height; j++)
            {
                result.setRGB(i, j, input[i + j * width]);
            }
        }
        File newFile = new File(fileName);
        ImageIO.write(result, "png", newFile);
    }


    public static void main(String[] args)
    {
        //输入图片名称和分辨率信息
        Scanner in = new Scanner(System.in);
        String filename = in.nextLine();
        int to_widith = in.nextInt();
        int to_height = in.nextInt();

        //读取图片文件
        File f = new File(filename);
        BufferedImage input;

        try {
            input = ImageIO.read(f);

            //线性插值和转换
            int[] input_array = get_one_array(input);
            int[] result = compute_result(input_array, input.getWidth(), input.getHeight(), to_widith, to_height);

            //存储
            save(to_widith, to_height, result, "output_scale.png");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

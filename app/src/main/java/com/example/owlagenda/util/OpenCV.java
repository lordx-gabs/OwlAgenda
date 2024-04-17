package com.example.owlagenda.util;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

public class OpenCV {
    public static ArrayList<Point> coordenadaBolinhas(String filePath) {
        // Carregar a biblioteca nativa do OpenCV
        OpenCVLoader.initLocal();
        // Carregar a imagem
        Mat imagemTeste = Imgcodecs.imread(filePath);

        Mat imagem = new Mat();
        Size size = new Size(90, 150);
        Imgproc.resize(imagemTeste, imagem, size);

        // Converter a imagem para escala de cinza
        Mat imagemGray = new Mat();
        Imgproc.cvtColor(imagem, imagemGray, Imgproc.COLOR_BGR2GRAY);

        // Aplicar um filtro gaussiano para suavizar a imagem
        Mat imagemBlur = new Mat();
        Imgproc.GaussianBlur(imagemGray, imagemBlur, new org.opencv.core.Size(5, 5), 0);

        // Aplicar uma operação de limiarização para segmentar as bolinhas pretas
        Mat imagemThreshold = new Mat();
        Imgproc.threshold(imagemBlur, imagemThreshold, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        // Encontrar contornos na imagem limiarizada
        List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(imagemThreshold, contornos, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Inicializar lista para armazenar coordenadas dos centros das bolinhas
        ArrayList<Point> coordenadasBolinhas = new ArrayList<>();

        // Iterar sobre os contornos encontrados
        for (MatOfPoint contorno : contornos) {
            double area = Imgproc.contourArea(contorno);
            if (area > 10) {  // Considerar apenas contornos significativos
                // Calcular o momento do contorno
                Moments momentos = Imgproc.moments(contorno);
                int centroX = (int) (momentos.get_m10() / momentos.get_m00());
                int centroY = (int) (momentos.get_m01() / momentos.get_m00());

                // Armazenar as coordenadas do centro da bolinha
                coordenadasBolinhas.add(new Point(centroX, centroY));

                // Desenhar um círculo no centro da bolinha para verificação visual (opcional)
                Imgproc.circle(imagem, new Point(centroX, centroY), 5, new Scalar(0, 255, 0), -1);
            }
        }

        // Exibir a imagem com as bolinhas pretas marcadas (opcional)
        Imgcodecs.imwrite("C:\\Users\\biels\\Downloads\\Teste_CatsDogs\\img\\resultado.jpg", imagem);
        return coordenadasBolinhas;
    }

    public static Integer compararArraysDeTuplas(List<Point> array1, List<Point> array2) {
        int margem = 10;

        // Verificar se os arrays têm o mesmo tamanho
        if (array1.size() != array2.size()) {
            System.out.println("Os arrays têm tamanhos diferentes. Não é possível comparar.");
            return null;
        }

        // Inicializar lista para armazenar os índices das tuplas iguais
        List<Integer> indicesIguais = new ArrayList<>();

        // Iterar sobre as tuplas nos arrays
        for (int i = 0; i < array1.size(); i++) {
            // Verificar se os números nas tuplas nos índices i são iguais ou estão dentro da margem
            if (Math.abs(array1.get(i).x - array2.get(i).x) <= margem &&
                    Math.abs(array1.get(i).y - array2.get(i).y) <= margem) {
                indicesIguais.add(i);
            }
        }

        return indicesIguais.size();
    }
}

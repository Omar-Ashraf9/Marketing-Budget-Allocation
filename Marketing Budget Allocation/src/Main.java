import java.io.IOException;
import java.util.*;

public class Main
{
    public static Scanner input = new Scanner(System.in);
    public static int marketingChannels , parentsNum = 6 , generationNum = 100;
    public static double totalBudget;
    public static Map <String, Double> returnOnInvestment = new LinkedHashMap<String, Double>();
    public static Map <String, Pair<Double,Double>> bounds = new LinkedHashMap<String, Pair<Double,Double>>();

    public static void takeInput() throws IOException {
        System.out.println("Enter the marketing budget (in thousands): ");
        totalBudget = input.nextInt();
        System.out.println("Enter the number of marketing channels: ");
        marketingChannels = input.nextInt();
        System.out.println("Enter the name and ROI (in %) of each channel separated by space: ");
        for (int i = 0; i < marketingChannels; i++)
        {
            String channelName = input.next();
            double percentage = input.nextInt();
            returnOnInvestment.put(channelName,percentage);
        }
        System.out.println("Enter the lower (k) and upper bounds (%) of investment in each channel: (enter x if there" +
                " is no bound): ");

        for (Map.Entry<String,Double> entry : returnOnInvestment.entrySet())
        {
            Pair <Double,Double> p = new Pair<Double, Double>();
            String lower = input.next();
            String upper = input.next();
            String channelName = entry.getKey();
            if(lower.equals("x"))
                p.setLeft(0.0);
            else
                p.setLeft(Double.parseDouble(lower));
            if(upper.equals("x"))
                p.setRight(totalBudget);
            else
                p.setRight((Double.parseDouble(upper) / 100) * totalBudget);
            bounds.put(channelName,p);
        }
        GeneticAlgorithm g = new GeneticAlgorithm();
        g.GA();
    }
    public static void main(String[] args) throws IOException {
        takeInput();
    }
}

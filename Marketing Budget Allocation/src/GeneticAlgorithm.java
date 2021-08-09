import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GeneticAlgorithm
{
    public static Vector<Vector<Double>> population;
    public static Vector<Vector<Double>> matingPool;
    public static Vector<Vector<Double>> offSpring;
    public static Vector<Double> fitValues;
    public static Vector<Double> bestSolution;
    public static double bestFitness = Double.MIN_VALUE;
    public static Vector<Vector<Double>> bestParents;
    public static FileWriter myWriter;

    static
    {
        try {
            myWriter = new FileWriter("output.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFeasible(Vector<Double> chromosome)
    {
         /* 1- check if the summation is <= the total budget.
           2- check if the lower is satisfied and the upper too.
         */
        double sum = 0.0;
        for (int i = 0; i < chromosome.size(); i++)
        {
            sum += chromosome.get(i);
        }
        if(sum > Main.totalBudget)
            return false;
        else
        {
            int idx = 0;
            for (Map.Entry<String, Pair<Double,Double>> entry : Main.bounds.entrySet())
            {
                if(chromosome.get(idx) < entry.getValue().getLeft() || chromosome.get(idx) > entry.getValue().getRight())
                {
                    return false;
                }
                idx++;
            }
        }
        return true;
    }
    public static boolean isDifferent()
    {
        /** utility function to check if all selected chromosomes are the same or not **/
        Vector <Double> toCheck = matingPool.get(0);
        for (int i = 1; i < Main.parentsNum; i++)
        {
            if(!toCheck.equals(matingPool.get(i)))
            {
                return true;
            }
        }
        return false;
    }
    public static void initialization()
    {
        population = new Vector<Vector<Double>>();
        for(int i = 0; i < Main.parentsNum; i++)
        {
            double sum = 0.0;
            Vector<Double> parent = new Vector<Double>();
            for (Map.Entry<String, Pair<Double, Double>> entry : Main.bounds.entrySet())
            {
                Pair<Double, Double> p = entry.getValue();
                double val = Math.random() * (p.getRight() - p.getLeft() + 1) + p.getLeft();
                sum += val;
                parent.add(val);
            }
            /** Handling infeasible solutions **/
            if(sum > Main.totalBudget)
                i--;
            else
            {
                population.add(parent);
            }
        }
    }
    public static void fitnessCalc()
    {
        fitValues = new Vector<Double>();
        bestParents = new Vector<Vector<Double>>();
        for (int i = 0; i < population.size(); i++)
        {
            double sum = 0.0;
            int idx = 0;
            for (Map.Entry<String, Double> entry : Main.returnOnInvestment.entrySet())
            {
                sum += (population.get(i).get(idx) * (entry.getValue() / 100));
                idx++;
            }
            String sValue = (String) String.format("%.2f", sum);
            Double newSum = Double.parseDouble(sValue);
            fitValues.add(newSum);
        }
        /** Keep the best parents to perform elitist replacement **/
        double largest = Double.MIN_VALUE ,  second = Double.MIN_VALUE;

        int bestIdx = -1 ,  secondBestIdx = -1;

        // Find the best parent
        for(int i = 0; i < fitValues.size(); i++)
        {
            largest = Math.max(largest, fitValues.get(i));
            bestIdx = i;
        }

        // Find the second best parent
        for(int i = 0; i < fitValues.size(); i++)
        {
            if (fitValues.get(i) != largest)
            {
                second = Math.max(second, fitValues.get(i));
                secondBestIdx = i;
            }
        }
        bestParents.add(population.get(bestIdx));
        if(Main.parentsNum % 2 == 0 && secondBestIdx == -1)
        {
            /* also keep the second best parent in the population */
            bestParents.add(population.get(bestIdx));
        }else if(Main.parentsNum % 2 == 0 && secondBestIdx != -1)
        {
            bestParents.add(population.get(secondBestIdx));
        }

    }
    public static void selection()
    {
        /** Apply tournament selection method to choose the appropriate parents **/
        int k = 2;
        matingPool = new Vector<Vector<Double>>();
        for(int i = 0; i < Main.parentsNum; i++)
        {
            double best = Double.MIN_VALUE;
            int r , bestIdx = -1;
            for(int j = 0; j < k; j++)
            {
                Random rand = new Random();
                r = rand.nextInt(Main.parentsNum);
                double individual = fitValues.get(r);
                if(individual > best)
                {
                    best = individual;
                    bestIdx = r;
                }
            }
            matingPool.add(population.get(bestIdx));
        }
    }
    public static void crossOver()
    {
        offSpring = new Vector<Vector<Double>>();
        Random rand = new Random();
        double pc = 0.6;
        int idxOfP1 , idxOfP2 , xc1 , xc2;


        int limit = ((Main.parentsNum % 2 != 0) ? Main.parentsNum/2 : (Main.parentsNum/2 - 1));
        for(int i = 0; i < limit; i++)
        {
            idxOfP1 = 0; idxOfP2 = 0;

            /** loop until we select two different parents **/
            while (matingPool.get(idxOfP1).equals(matingPool.get(idxOfP2)))
            {
                idxOfP1 = rand.nextInt(Main.parentsNum);
                idxOfP2 = rand.nextInt(Main.parentsNum);
            }
            Vector<Double> parent1 = matingPool.get(idxOfP1);
            Vector<Double> parent2 = matingPool.get(idxOfP2);
            double rc = rand.nextDouble();
            if(rc <= pc)
            {
                 /** crossover occurs

                 P1: 1 2 1 1 0
                 P2: 2 0 1 1 1

                 child1:  1 | 0 1 1 1
                 child2:  2 | 2 1 1 0

                 **/

                /** Generate a random number to determine at which point we will perform crossover **/
                xc1 = 0; xc2 = 0;
                while (xc1 == xc2)
                {
                    xc1 = (int)(Math.random() * ((Main.marketingChannels-1) - (1) + 1) + 1);
                    xc2 = (int)(Math.random() * ((Main.marketingChannels-1) - (1) + 1) + 1);
                }
                if(xc1 > xc2)
                {
                    int t1 = xc2;
                    xc2 = xc1;
                    xc1 = t1;
                }


                List child1fromP1 = parent1.subList(0,xc1);
                List child1fromP2 = parent2.subList(xc1,xc2);
                List lastPart1     = parent1.subList(xc2,Main.marketingChannels);


                Vector<Double> finalChild1 = new Vector<Double>();
                finalChild1.addAll(child1fromP1);
                finalChild1.addAll(child1fromP2);
                finalChild1.addAll(lastPart1);
                if(!isFeasible(finalChild1))
                {
                    offSpring.add(parent1);
                }else
                    offSpring.add(finalChild1);

                List child2fromP2 =  parent2.subList(0,xc1);
                List child2fromP1 =  parent1.subList(xc1,xc2);
                List lastPart2    =  parent2.subList(xc2,Main.marketingChannels);

                Vector<Double> finalChild2 = new Vector<Double>();
                finalChild2.addAll(child2fromP2);
                finalChild2.addAll(child2fromP1);
                finalChild2.addAll(lastPart2);

                if(!isFeasible(finalChild2))
                {
                    offSpring.add(parent2);
                }else
                    offSpring.add(finalChild2);
            }else
            {
                /** add the parents to offspring **/
                offSpring.add(parent1);
                offSpring.add(parent2);
            }
        }
    }

    public static void uniForm_Mutation()
    {
        Random rand = new Random();
        double pm = 0.05;
        for(int i = 0; i < offSpring.size(); i++)
        {
            int idx = 0;
            for (Map.Entry<String, Pair<Double, Double>> entry : Main.bounds.entrySet())
            {
                double r = rand.nextDouble();
                /** if we will perform mutation **/
                if(r <= pm)
                {
                    Pair <Double , Double> p = entry.getValue();
                    double dLower = offSpring.get(i).get(idx) - p.getLeft();
                    double dUpper = p.getRight() - offSpring.get(i).get(idx);
                    double d;
                    double r1 = rand.nextDouble();
                    if(r1 <= 0.5)
                        d = dLower;
                    else
                        d = dUpper;
                    double r2 = Math.random() * (d - 0 + 1) + 0;
                    if(d == dLower)
                    {
                        offSpring.get(i).set(idx, offSpring.get(i).get(idx) - r2);
                        if(!isFeasible(offSpring.get(i)))
                        {
                            offSpring.get(i).set(idx, offSpring.get(i).get(idx) + r2);
                        }
                    }else
                    {
                        offSpring.get(i).set(idx, offSpring.get(i).get(idx) + r2);
                        if(!isFeasible(offSpring.get(i)))
                        {
                            offSpring.get(i).set(idx, offSpring.get(i).get(idx) - r2);
                        }
                    }
                }
                idx++;
            }
        }
    }


    public static void nonUniform_Mutation(int t)
    {
        Random rand = new Random();
        double pm = 0.05 , b = 0.7;

        for(int i = 0; i < offSpring.size(); i++)
        {
            int idx = 0;
            for (Map.Entry<String, Pair<Double, Double>> entry : Main.bounds.entrySet())
            {
                double r = rand.nextDouble();
                /** if we will perform mutation **/
                if(r <= pm)
                {
                    Pair <Double , Double> p = entry.getValue();
                    double dLower = offSpring.get(i).get(idx) - p.getLeft();
                    double dUpper = p.getRight() - offSpring.get(i).get(idx);
                    double y , dTY;
                    double r1 = rand.nextDouble();
                    if(r1 <= 0.5)
                        y = dLower;
                    else
                        y = dUpper;
                    double rr = rand.nextDouble();
                    dTY = y * (1 - Math.pow(r,Math.pow(1-t/Main.generationNum,b)));
                    if(y == dLower)
                    {
                        offSpring.get(i).set(idx, offSpring.get(i).get(idx) - dTY);
                        if(!isFeasible(offSpring.get(i)))
                        {
                            offSpring.get(i).set(idx, offSpring.get(i).get(idx) + dTY);
                        }
                    }else
                    {
                        offSpring.get(i).set(idx, offSpring.get(i).get(idx) + dTY);
                        if(!isFeasible(offSpring.get(i)))
                        {
                            offSpring.get(i).set(idx, offSpring.get(i).get(idx) - dTY);
                        }
                    }
                }
                idx++;
            }
        }
    }

    public static void elitist_Replacement()
    {
        population = new Vector<Vector<Double>>();
        population.addAll(bestParents);
        population.addAll(offSpring);

    }
    public static void printToFile() throws IOException
    {
        int idx = fitValues.indexOf(Collections.max(fitValues));
        int j = 0;
        Vector <Double> res = population.get(idx);
        myWriter.write("The final marketing budget allocation is: "  + "\n");
        for (Map.Entry<String,Double> entry : Main.returnOnInvestment.entrySet())
        {
            String sValue = (String) String.format("%.2f", res.get(j));
            Double newRes = Double.parseDouble(sValue);
            myWriter.write(entry.getKey() + " -> " + newRes + "k" + "\n");
            j++;
        }
        myWriter.write("The total profit is " + fitValues.get(fitValues.indexOf(Collections.max(fitValues))) + "k" + "\n");
        myWriter.write("-------------------------------------------"  + "\n");
    }
    public static void printToConsole(Vector<Double> res, Double totalProfit) throws IOException
    {
        int j = 0;
        System.out.println("The final marketing budget allocation is: ");
        for (Map.Entry<String,Double> entry : Main.returnOnInvestment.entrySet())
        {
            String sValue = (String) String.format("%.2f", res.get(j));
            Double newRes = Double.parseDouble(sValue);
            System.out.println(entry.getKey() + " -> " + newRes + "k");
            j++;
        }
        System.out.println("The total profit is " + totalProfit + "k");
    }
    public static void GA() throws IOException
    {
        for (int j = 0; j < 20; j++)
        {
            initialization();
            for (int i = 0; i < Main.generationNum; i++)
            {
                fitnessCalc();

                selection();

                /** if the mating pool all the same go back and re-calculate this iteration. **/
                if (!isDifferent()) {
                    --i;
                    continue;
                }

                crossOver();

                //uniForm_Mutation();
                nonUniform_Mutation(i);

                elitist_Replacement();
            }
            if(bestFitness < fitValues.get(fitValues.indexOf(Collections.max(fitValues))))
            {
                bestFitness = fitValues.get(fitValues.indexOf(Collections.max(fitValues)));
                bestSolution = population.get(fitValues.indexOf(Collections.max(fitValues)));
            }
            printToFile();
        }
        myWriter.close();
        printToConsole(bestSolution, bestFitness);
    }

}

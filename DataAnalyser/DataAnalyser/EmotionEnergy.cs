using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DataAnalyser
{
    public class EmotionEnergy
    {
        public double Arousal { get; set; }

        public double Valency { get; set; }

        public EmotionEnergy(double arousal, double valency)
        {
            Arousal = arousal;
            Valency = valency;
        }

        public EmotionQuadrant Quadrant
        {
            get
            {
                if (Arousal >= 0 && Valency <= 0)
                    return EmotionQuadrant.HighEnergyLowValency;
                else if (Arousal >= 0 && Valency >= 0)
                    return EmotionQuadrant.HighEnergyHighValency;
                else if (Arousal <= 0 && Valency >= 0)
                    return EmotionQuadrant.LowEnergyHighValency;
                else
                    return EmotionQuadrant.LowEnergyLowValency;
            }
        }


        public override string ToString()
        {
            return string.Format("{0:0.00}, {1:0.00}", Valency, Arousal);
        }
    }
}
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

        public override string ToString()
        {
            return string.Format("{0:0.00}, {1:0.00}", Valency, Arousal);
        }
    }
}
using System;
using System.Collections.Generic;
using System.Linq;

namespace DataAnalyser
{
    public class Session
    {
        public SessionData[] Data { get; set; }

        public SessionTimeMarker[] TimeMarkers { get; set; }

        List<EmotionEnergy> _phase2CalmReadings = null;
        List<EmotionEnergy> _phase2AngryReadings = null;
        List<EmotionEnergy> _phase2HappyReadings = null;
        List<EmotionEnergy> _phase2SadReadings = null;

        public EmotionEnergy[] Phase2CalmReadings
        {
            get
            {
                if (_phase2CalmReadings == null)
                    SetupPhase2Readings();
                return _phase2CalmReadings.ToArray();
            }
        }

        public EmotionEnergy[] Phase2SadReadings
        {
            get
            {
                if (_phase2SadReadings == null)
                    SetupPhase2Readings();
                return _phase2SadReadings.ToArray();
            }
        }

        public EmotionEnergy[] Phase2AngryReadings
        {
            get
            {
                if (_phase2AngryReadings == null)
                    SetupPhase2Readings();
                return _phase2AngryReadings.ToArray();
            }
        }

        public EmotionEnergy[] Phase2HappyReadings
        {
            get
            {
                if (_phase2HappyReadings == null)
                    SetupPhase2Readings();
                return _phase2HappyReadings.ToArray();
            }
        }
        public EmotionEnergy Phase2CalmAverage
        {
            get
            {
                return GetAverageEmotion(Phase2CalmReadings);
            }
        }

        public EmotionEnergy Phase2AngryAverage
        {
            get
            {
                return GetAverageEmotion(Phase2AngryReadings);
            }
        }

        public EmotionEnergy Phase2HappyAverage
        {
            get
            {
                return GetAverageEmotion(Phase2HappyReadings);
            }
        }

        public EmotionEnergy Phase2SadAverage
        {
            get
            {
                return GetAverageEmotion(Phase2SadReadings);
            }
        }

        public static EmotionEnergy GetAverageEmotion(EmotionEnergy[] energies)
        {
            double arousal = 0;
            double valency = 0;
            foreach (EmotionEnergy e in energies)
            {
                arousal += e.Arousal;
                valency += e.Valency;
            }
            return new EmotionEnergy(arousal / energies.Length, valency / energies.Length);
        }


        private void SetupPhase2Readings()
        {
            _phase2CalmReadings = new List<EmotionEnergy>();
            _phase2AngryReadings = new List<EmotionEnergy>();
            _phase2SadReadings = new List<EmotionEnergy>();
            _phase2HappyReadings = new List<EmotionEnergy>();
            for (int i = 1; i <= 20; i++)
            {
                string state = GetData("Phase2Experiment.State" + i + ".State");
                double arousal = double.Parse(GetData("Phase2Experiment.State" + i + ".Energy"));
                double valency = double.Parse(GetData("Phase2Experiment.State" + i + ".Pleasantness"));
                if (state == "Calm")
                    _phase2CalmReadings.Add(new EmotionEnergy(arousal, valency));
                else if (state == "Angry")
                    _phase2AngryReadings.Add(new EmotionEnergy(arousal, valency));
                else if (state == "Happy")
                    _phase2HappyReadings.Add(new EmotionEnergy(arousal, valency));
                else if (state == "Sad")
                    _phase2SadReadings.Add(new EmotionEnergy(arousal, valency));
            }
        }

        public string SessionId { get; set; }

        public Guid Uuid { get; set; }

        public string GetData(string key)
        {
            return Data.FirstOrDefault(d => d.Key == key)?.Value;
        }
    }
}
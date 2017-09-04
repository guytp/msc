using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
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


        public double Phase2AngryConsistency
        {
            get
            {
                return GetEmotionalReadingConsistency(Phase2AngryReadings);
            }
        }


        public double Phase2CalmConsistency
        {
            get
            {
                return GetEmotionalReadingConsistency(Phase2CalmReadings);
            }
        }


        public double Phase2HappyConsistency
        {
            get
            {
                return GetEmotionalReadingConsistency(Phase2HappyReadings);
            }
        }

        public double Ipip => double.Parse(GetData("IntroIpip.FinalScore"));

        public bool IsIpipAbnormal => Ipip < 1.5 || Ipip > 3.5;


        public double Phase2SadConsistency
        {
            get
            {
                return GetEmotionalReadingConsistency(Phase2SadReadings);
            }
        }

        public string DemographicsAge => GetData("Demographics.Age");
        public string DemographicsGender => GetData("Demographics.Gender");
        public string DemographicsEthnicity => GetData("Demographics.Ethnicity");
        public string DemographicsEducation => GetData("Demographics.Education");
        public string[] DemographicsStudyWork
        {
            get
            {
                string[] res = new string[int.Parse(GetData("Demographics.ResearchStudyWork.Count"))];
                for (int i = 0; i < res.Length; i++)
                    res[i] = GetData("Demographics.ResearchStudyWork." + (i + 1));
                return res;
            }
        }


        public string[] Phase3HighEnergyHighValencyWords = new string[] { "Excited", "Happy", "Surprised", "Delighted", "Cheerful" };
        public string[] Phase3LowEnergyHighValencyWords = new string[] { "Relaxed", "Calm", "Serene", "Content", "Pleased" };  // Low Energy, Pleasant
        public string[] Phase3LowEnergyLowValencyWords = new string[] { "Sad", "Depressed", "Gloomy", "Bored", "Tired" }; // Low Energy, Unpleasant
        public string[] Phase3HighEnergyLowValencyWords = new string[] { "Afraid", "Angry", "Annoyed", "Frustrated", "Terrified" }; // High Energy, Unpleasant

        public EmotionQuadrant Phase3GetQuadrantForWord(string word)
        {
            int wordNumber = -1;
            for (int i = 0; i < 20; i++)
            {
                string thisWord = GetData("Phase3Experiment.Word" + (i + 1));
                if (thisWord == word)
                {
                    wordNumber = i;
                    break;
                }
            }
            if (wordNumber == -1)
                throw new Exception("Not found");

            string selectedState = GetData("Phase3Experiment.Word" + wordNumber + ".Selection");
            switch (selectedState)
            {
                case "Calm":
                    return EmotionQuadrant.LowEnergyHighValency;
                case "Angry":
                    return EmotionQuadrant.HighEnergyLowValency;
                case "Happy":
                    return EmotionQuadrant.HighEnergyHighValency;
                case "Sad":
                    return EmotionQuadrant.LowEnergyLowValency;
                default:
                    return EmotionQuadrant.None;
            }
        }

        public EmotionQuadrant[] GetPhase3QuadrantsForState(EmotionQuadrant state)
        {
            List<EmotionQuadrant> returnValues = new List<EmotionQuadrant>();
            string[] words;
            switch (state)
            {
                case EmotionQuadrant.HighEnergyLowValency:
                    words = Phase3HighEnergyLowValencyWords;
                    break;
                case EmotionQuadrant.HighEnergyHighValency:
                    words = Phase3HighEnergyHighValencyWords;
                    break;
                case EmotionQuadrant.LowEnergyHighValency:
                    words = Phase3LowEnergyHighValencyWords;
                    break;
                case EmotionQuadrant.LowEnergyLowValency:
                    words = Phase3LowEnergyLowValencyWords;
                    break;
                default:
                    throw new Exception("Not a valid state");
            }
            foreach (string word in words)
            {
                EmotionQuadrant quadrant = Phase3GetQuadrantForWord(word);
                if (quadrant != EmotionQuadrant.None)
                    returnValues.Add(quadrant);
            }
            return returnValues.ToArray();
        }

        public double Phase2OverallConsistency => (Phase2AngryConsistency + Phase2CalmConsistency + Phase2SadConsistency + Phase2HappyConsistency) / 4f;

        public bool Phase2IsConsistent => Phase2OverallConsistency >= 80;

        private double GetEmotionalReadingConsistency(EmotionEnergy[] energies)
        {
            Dictionary<EmotionQuadrant, int> dic = new Dictionary<EmotionQuadrant, int>();
            dic.Add(EmotionQuadrant.HighEnergyHighValency, energies.Count(ee => ee.Quadrant == EmotionQuadrant.HighEnergyHighValency));
            dic.Add(EmotionQuadrant.HighEnergyLowValency, energies.Count(ee => ee.Quadrant == EmotionQuadrant.HighEnergyLowValency));
            dic.Add(EmotionQuadrant.LowEnergyHighValency, energies.Count(ee => ee.Quadrant == EmotionQuadrant.LowEnergyHighValency));
            dic.Add(EmotionQuadrant.LowEnergyLowValency, energies.Count(ee => ee.Quadrant == EmotionQuadrant.LowEnergyLowValency));

            int highestCount = 0;
            foreach (EmotionQuadrant e in Enum.GetValues(typeof(EmotionQuadrant)).Cast<EmotionQuadrant>().Where(e => e != EmotionQuadrant.None))
                if (dic[e] > highestCount)
                    highestCount = dic[e];
            return Math.Round((double)highestCount / (double)energies.Length * 100f, 2);
        }

        public string[] GetPhase3CorrectQuadrantIdentifiedWords()
        {
            List<string> correctWords = new List<string>();
            foreach (string[] wordList in new string[][] { Phase3HighEnergyHighValencyWords, Phase3HighEnergyLowValencyWords, Phase3LowEnergyHighValencyWords, Phase3LowEnergyLowValencyWords })
            {
                EmotionQuadrant intendedQuadrant;
                if (wordList == Phase3HighEnergyHighValencyWords)
                    intendedQuadrant = EmotionQuadrant.HighEnergyHighValency;
                else if (wordList == Phase3HighEnergyLowValencyWords)
                    intendedQuadrant = EmotionQuadrant.HighEnergyLowValency;
                else if (wordList == Phase3LowEnergyHighValencyWords)
                    intendedQuadrant = EmotionQuadrant.LowEnergyHighValency;
                else
                    intendedQuadrant = EmotionQuadrant.LowEnergyLowValency;
                foreach (string word in wordList)
                    if (Phase3GetQuadrantForWord(word) == intendedQuadrant)
                        correctWords.Add(word);
            }
            return correctWords.ToArray();
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

        string[] _panasPositiveWords = new string[] { "Interested", "Excited", "Strong", "Enthusiastic", "Proud", "Alert", "Inspired", "Determined", "Attentive", "Active" };
        string[] _panasNegativeWords = new string[] { "Distressed", "Upset", "Guilty", "Scared", "Hostile", "Irritable", "Ashamed", "Nervous", "Jittery", "Afraid" };
        string[] _panasScores = new[] { "Very Slighty or Not at All", "A Little", "Moderately", "Quite a Bit", "Extremely" };

        public double PanasIntroPositive
        {
            get
            {
                return GetPanasScore(_panasPositiveWords, "IntroPanas");
            }
        }
        public double PanasIntroNegative
        {
            get
            {
                return GetPanasScore(_panasNegativeWords, "IntroPanas");
            }
        }
        public double PanasOutroPositive
        {
            get
            {
                return GetPanasScore(_panasPositiveWords, "OutroPanas");
            }
        }
        public double PanasOutroNegative
        {
            get
            {
                return GetPanasScore(_panasNegativeWords, "OutroPanas");
            }
        }

        public double PanasDeltaPositive
        {
            get
            {
                return PanasOutroPositive - PanasIntroPositive;
            }
        }
        public double PanasDeltaNegative
        {
            get
            {
                return PanasOutroNegative - PanasIntroNegative;
            }
        }

        private double GetPanasScore(string[] words, string section)
        {
            int totalScore = 0;
            foreach (string word in words)
            {
                string selected = GetData(section + "." + word);
                int score = 0;
                for (int i = 0; i < 5; i++)
                    if (_panasScores[i] == selected)
                    {
                        score = i + 1;
                        break;
                    }
                if (score == 0)
                    throw new Exception("Invalid panas data");
                totalScore += score;
            }
            return totalScore;
        }


        private bool _phase1Done = false;
        private double _phase1CorrectPercentage;
        private int _phase1InvolvingAngryCorrect;
        private int _phase1InvolvingAngryIncorrect;
        private int _phase1InvolvingCalmCorrect;
        private int _phase1InvolvingCalmIncorrect;
        private int _phase1InvolvingHappyCorrect;
        private int _phase1InvolvingHappyIncorrect;
        private int _phase1InvolvingSadCorrect;
        private int _phase1InvolvingSadIncorrect;
        public double Phase1CorrectPercentage
        {
            get
            {
                if (!_phase1Done)
                    SetupPhase1Readings();
                return _phase1CorrectPercentage;
            }
        }
        public double Phase1InvolvingAngryCorrect
        {
            get
            {
                if (!_phase1Done)
                    SetupPhase1Readings();
                return _phase1InvolvingAngryCorrect;
            }
        }
        public double Phase1InvolvingAngryIncorrect
        {
            get
            {
                if (!_phase1Done)
                    SetupPhase1Readings();
                return _phase1InvolvingAngryIncorrect;
            }
        }
        public double Phase1InvolvingCalmCorrect
        {
            get
            {
                if (!_phase1Done)
                    SetupPhase1Readings();
                return _phase1InvolvingCalmCorrect;
            }
        }
        public double Phase1InvolvingCalmIncorrect
        {
            get
            {
                if (!_phase1Done)
                    SetupPhase1Readings();
                return _phase1InvolvingCalmIncorrect;
            }
        }
        public double Phase1InvolvingSadCorrect
        {
            get
            {
                if (!_phase1Done)
                    SetupPhase1Readings();
                return _phase1InvolvingSadCorrect;
            }
        }
        public double Phase1InvolvingSadIncorrect
        {
            get
            {
                if (!_phase1Done)
                    SetupPhase1Readings();
                return _phase1InvolvingSadIncorrect;
            }
        }
        public double Phase1InvolvingHappyCorrect
        {
            get
            {
                if (!_phase1Done)
                    SetupPhase1Readings();
                return _phase1InvolvingHappyCorrect;
            }
        }
        public double Phase1InvolvingHappyIncorrect
        {
            get
            {
                if (!_phase1Done)
                    SetupPhase1Readings();
                return _phase1InvolvingHappyIncorrect;
            }
        }

        public bool SitWithItIsAngry
        {
            get
            {
                return GetData("Phase3Cushion.State.Chosen") == "Angry";
            }
        }

        public bool SitWithItCorrectIdentification
        {
            get
            {
                return GetData("Phase3Cushion.State.Chosen") == GetData("Phase3Cushion.State.DisplayAs");
            }
        }

        public DateTime SitWithItStartTime
        {
            get
            {
                return GetDate("Phase3Cushion", "Show");
            }
        }

        public DateTime StartTime
        {
            get
            {
                return GetDate("IntroWelcome", "Show");
            }
        }

        private void SetupPhase1Readings()
        {
            int correctAnswers = 0;
            for (int i = 1; i <= 19; i += 2)
            {
                string states = GetData("Phase1Experiment.State" + i + "-" + (i + 1) + ".States");
                bool answerSame = GetData("Phase1Experiment.State" + i + "-" + (i + 1) + ".Answer") == "Same";
                string firstState = (states.Split(new string[] { "-" }, StringSplitOptions.RemoveEmptyEntries)[0]).Replace(" ", "");
                string secondState = (states.Split(new string[] { "-" }, StringSplitOptions.RemoveEmptyEntries)[1]).Replace(" ", "");
                bool areSame = firstState == secondState;
                bool isMatch = areSame == answerSame;
                if (isMatch)
                    correctAnswers++;
                if (firstState == "Angry" || secondState == "Angry")
                    if (isMatch)
                        _phase1InvolvingAngryCorrect++;
                    else
                        _phase1InvolvingAngryIncorrect++;
                if (firstState == "Calm" || secondState == "Calm")
                    if (isMatch)
                        _phase1InvolvingCalmCorrect++;
                    else
                        _phase1InvolvingCalmIncorrect++;
                if (firstState == "Happy" || secondState == "Happy")
                    if (isMatch)
                        _phase1InvolvingHappyCorrect++;
                    else
                        _phase1InvolvingHappyIncorrect++;
                if (firstState == "Sad" || secondState == "Sad")
                    if (isMatch)
                        _phase1InvolvingSadCorrect++;
                    else
                        _phase1InvolvingSadIncorrect++;
                //Phase1Experiment.State1 - 2.States", "Value": "Angry - Angry"}, {"Key": "Phase1Experiment.State1 - 2.Answer", "Value": "Different"}
            }
            _phase1CorrectPercentage = correctAnswers / 10f * 100;
            _phase1Done = true;
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
        public DateTime GetDate(string category, string action)
        {
            string value = TimeMarkers.FirstOrDefault(tm => tm.Category == category && tm.Action == action)?.Date;
            if (value == null)
                return new DateTime(0);

            DateTime dt;
            if (DateTime.TryParseExact(value, "yyyyMMddHHmmss.fff", CultureInfo.InvariantCulture, DateTimeStyles.None, out dt))
                return dt.Subtract(TimeSpan.FromSeconds(43));
            return new DateTime(0);
        }

        public EdaReading[] EdaReadings { get; set; }

        public double GetEdaReadingAverage(DateTime startTime, DateTime endTime)
        {
            if (EdaReadings == null)
                return 0;
            IEnumerable<EdaReading> readings = EdaReadings.Where(eda => eda.Time >= startTime && eda.Time <= endTime);
            if (readings.Count() == 0)
                return 0;
            double avg = readings.Average(rdg => rdg.Value);
            return avg;
        }

        public double SitWithItStartEda
        {
            get
            {
                double value = GetEdaReadingAverage(SitWithItStartTime.AddSeconds(-15), SitWithItStartTime.AddSeconds(-10));
                return value;
            }
        }
        public double SitWithItEndEda
        {
            get
            {
                double value = GetEdaReadingAverage(SitWithItStartTime.AddSeconds(65), SitWithItStartTime.AddSeconds(70));
                return value;
            }
        }

        public double SitWithItEdaDelta
        {
            get
            {
                return SitWithItEndEda == 0 || SitWithItStartEda == 0 ? 0 : SitWithItEndEda - SitWithItStartEda;
            }
        }

        public double SitWithItEdaDeltaPercent
        {
            get
            {
                return SitWithItEdaDelta == 0 ? 0 : SitWithItEdaDelta / SitWithItStartEda * 100f;
            }
        }

        public Session()
        {

            string[] e4DataFolders = Directory.GetDirectories(@"C:\Users\guytp\Desktop\data\e4");
            List<EdaReading> readings = new List<EdaReading>();
            foreach (string folder in e4DataFolders)
            {
                string edaFile = Path.Combine(folder, "EDA.csv");
                if (!File.Exists(edaFile))
                    continue;
                string[] text = File.ReadAllLines(edaFile);
                if (text.Length < 3)
                    continue;
                DateTime timestamp = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc).AddSeconds(3600 + double.Parse(text[0]));
                double rate = double.Parse(text[1]);
                double msIncrement = 1000f / rate;
                for (int i = 2; i < text.Length; i++)
                {
                    double reading = double.Parse(text[i]);
                    readings.Add(new EdaReading { Time = timestamp, Value = reading });
                    timestamp = timestamp.AddMilliseconds(msIncrement);
                }
            }
            EdaReadings = readings.OrderBy(e => e.Time).ToArray();
        }
    }
}
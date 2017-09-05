using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Windows;

namespace DataAnalyser
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        List<Session> _sessions = new List<Session>();


        public MainWindow()
        {
            InitializeComponent();

            // Load data
            string[] dataFiles = Directory.GetFiles(@"C:\Users\guytp\Desktop\data", "*.json");

            foreach (string dataFile in dataFiles)
            {
                Session s = JsonConvert.DeserializeObject<Session>(File.ReadAllText(dataFile));
                if (s.StartTime < new DateTime(2017, 08, 09))
                    continue;
                _sessions.Add(s);
            }


            int vibrationCount = 0;
            int lightCount = 0;

            int phase3HappyCount = 0;
            int phase3SadCount = 0;
            int phase3CalmCount = 0;
            int phase3AngryCount = 0;

            int phase2AngryCorrectQuadrantCount = 0;
            int phase2AngryTotalQuadrantCount = 0;
            int phase2CalmCorrectQuadrantCount = 0;
            int phase2CalmTotalQuadrantCount = 0;
            int phase2HappyCorrectQuadrantCount = 0;
            int phase2HappyTotalQuadrantCount = 0;
            int phase2SadCorrectQuadrantCount = 0;
            int phase2SadTotalQuadrantCount = 0;

            List<EmotionEnergy> phase2HappyValues = new List<EmotionEnergy>();
            List<EmotionEnergy> phase2SadValues = new List<EmotionEnergy>();
            List<EmotionEnergy> phase2CalmValues = new List<EmotionEnergy>();
            List<EmotionEnergy> phase2AngryValues = new List<EmotionEnergy>();

            int phase2AngryAverageCorrectQuadrant = 0;
            int phase2SadAverageCorrectQuadrant = 0;
            int phase2CalmAverageCorrectQuadrant = 0;
            int phase2HappyAverageCorrectQuadrant = 0;

            int satWithAngryCount = 0;
            List<double> calmEda = new List<double>();
            List<double> angryEda = new List<double>();

            Dictionary<string, int> p3CorrectCounts = new Dictionary<string, int>();
            Dictionary<string, int> p3HeHvCounts = new Dictionary<string, int>();
            Dictionary<string, int> p3HeLvCounts = new Dictionary<string, int>();
            Dictionary<string, int> p3LeHvCounts = new Dictionary<string, int>();
            Dictionary<string, int> p3LeLvCounts = new Dictionary<string, int>();

            StringBuilder sb = new StringBuilder();
            sb.AppendLine(CsvLine.Header.ToString());

            foreach (Session session in _sessions.OrderBy(s => s.StartTime))
            {
                sb.AppendLine(new CsvLine(session).ToString());

                // Did they prefer vibration or light?
                string vibrationOrLight = session.GetData("Phase3.VibrationOrLight");
                if (vibrationOrLight == "Vibration")
                    vibrationCount++;
                else if (vibrationOrLight == "Light")
                    lightCount++;

                // Phase 3 words - where did they classify each state?
                for (int i = 1; i <= 20; i++)
                {
                    string word = session.GetData("Phase3Experiment.Word" + i);
                    string selection = session.GetData("Phase3Experiment.Word" + i + ".Selection");
                    if (word == selection)
                    {
                        if (word == "Happy")
                            phase3HappyCount++;
                        else if (word == "Sad")
                            phase3SadCount++;
                        else if (word == "Calm")
                            phase3CalmCount++;
                        else if (word == "Angry")
                            phase3AngryCount++;
                    }
                }

                if (session.Phase2AngryAverage.Quadrant == EmotionQuadrant.HighEnergyLowValency)
                    phase2AngryAverageCorrectQuadrant++;
                if (session.Phase2CalmAverage.Quadrant == EmotionQuadrant.LowEnergyHighValency)
                    phase2CalmAverageCorrectQuadrant++;
                if (session.Phase2HappyAverage.Quadrant == EmotionQuadrant.HighEnergyHighValency)
                    phase2HappyAverageCorrectQuadrant++;
                if (session.Phase2SadAverage.Quadrant == EmotionQuadrant.LowEnergyLowValency)
                    phase2SadAverageCorrectQuadrant++;

                phase2AngryValues.AddRange(session.Phase2AngryReadings);
                phase2HappyValues.AddRange(session.Phase2HappyReadings);
                phase2CalmValues.AddRange(session.Phase2CalmReadings);
                phase2SadValues.AddRange(session.Phase2SadReadings);

                foreach (EmotionEnergy e in session.Phase2AngryReadings)
                {
                    if (e.Quadrant == EmotionQuadrant.HighEnergyLowValency)
                        phase2AngryCorrectQuadrantCount++;
                    phase2AngryTotalQuadrantCount++;
                }
                foreach (EmotionEnergy e in session.Phase2CalmReadings)
                {
                    if (e.Quadrant == EmotionQuadrant.LowEnergyHighValency)
                        phase2CalmCorrectQuadrantCount++;
                    phase2CalmTotalQuadrantCount++;
                }
                foreach (EmotionEnergy e in session.Phase2SadReadings)
                {
                    if (e.Quadrant == EmotionQuadrant.LowEnergyLowValency)
                        phase2SadCorrectQuadrantCount++;
                    phase2SadTotalQuadrantCount++;
                }
                foreach (EmotionEnergy e in session.Phase2HappyReadings)
                {
                    if (e.Quadrant == EmotionQuadrant.HighEnergyHighValency)
                        phase2HappyCorrectQuadrantCount++;
                    phase2HappyTotalQuadrantCount++;
                }

                //Trace.WriteLine("Sit With: " + (session.SitWithItIsAngry ? "Angry" : "Calm ") + "   Correct Identification: " + session.SitWithItCorrectIdentification + "   Time: " + session.SitWithItStartTime + "   Session: " + session.StartTime + "    EDA: " + session.SitWithItStartEda + " -> " + session.SitWithItEndEda + " (" + session.SitWithItEdaDeltaPercent + "%)");
                if (session.SitWithItIsAngry)
                {
                    satWithAngryCount++;
                    if (session.SitWithItStartEda > 0 && session.SitWithItEdaDeltaPercent > -20 && session.SitWithItEdaDeltaPercent < 20)
                        angryEda.Add(session.SitWithItEdaDeltaPercent);
                }
                else if (session.SitWithItStartEda > 0 && session.SitWithItEdaDeltaPercent > -20 && session.SitWithItEdaDeltaPercent < 20)
                    calmEda.Add(session.SitWithItEdaDeltaPercent);

                Trace.WriteLine(session.StartTime);
                Trace.WriteLine("Panas P: " + session.PanasIntroPositive + " -> " + session.PanasOutroPositive + " (" + session.PanasDeltaPositive + ")     N: " + session.PanasIntroNegative + " -> " + session.PanasOutroNegative + " (" + session.PanasDeltaNegative + ")    " + (session.SitWithItIsAngry ? "Angry" : "Calm"));
                Trace.WriteLine("Phase 1 Correct: " + session.Phase1CorrectPercentage + " %    H: " + session.Phase1InvolvingHappyCorrect + "/" + session.Phase1InvolvingHappyIncorrect + "    A: " + session.Phase1InvolvingAngryCorrect + "/" + session.Phase1InvolvingAngryIncorrect + "    S: " + session.Phase1InvolvingSadCorrect + "/" + session.Phase1InvolvingSadIncorrect + "    C: " + session.Phase1InvolvingCalmCorrect + "/" + session.Phase1InvolvingCalmIncorrect);
                Trace.WriteLine("P2 Consistency S: " + session.Phase2SadConsistency + "   C: " + session.Phase2CalmConsistency + "    H: " + session.Phase2HappyConsistency + "    A: " + session.Phase2AngryConsistency + "    " + (session.Phase2IsConsistent ? "Consistent" : "          ") + "     = " + session.Phase2OverallConsistency);
                Trace.WriteLine("P1 Correct: " + session.Phase1CorrectPercentage + "    P2: " + session.Phase2IsConsistent + "      IPIP: " + (session.IsIpipAbnormal ? "Abnormal" : "Normal"));
                string str = "";
                foreach (string word in session.GetPhase3CorrectQuadrantIdentifiedWords())
                {
                    if (!p3CorrectCounts.ContainsKey(word))
                        p3CorrectCounts.Add(word, 1);
                    else
                        p3CorrectCounts[word]++;
                    str += (str == "" ? "" : ", ") + word;
                }
                foreach (string[] words in new string[][] { session.Phase3HighEnergyHighValencyWords, session.Phase3HighEnergyLowValencyWords, session.Phase3LowEnergyHighValencyWords, session.Phase3LowEnergyLowValencyWords })
                    foreach (string word in words)
                    {
                        EmotionQuadrant quadrant = session.Phase3GetQuadrantForWord(word);
                        Dictionary<string, int> dic;
                        switch (quadrant)
                        {
                            case EmotionQuadrant.HighEnergyLowValency:
                                dic = p3HeLvCounts;
                                break;
                            case EmotionQuadrant.HighEnergyHighValency:
                                dic = p3HeHvCounts;
                                break;
                            case EmotionQuadrant.LowEnergyHighValency:
                                dic = p3LeHvCounts;
                                break;
                            case EmotionQuadrant.LowEnergyLowValency:
                                dic = p3LeLvCounts;
                                break;
                            default:
                                continue;
                        }
                        if (!dic.ContainsKey(word))
                            dic.Add(word, 1);
                        else
                            dic[word]++;
                    }
                Trace.WriteLine("P3 Correct: " + str);
                Trace.WriteLine("");
            }

            EmotionEnergy phase2HappyAverage = Session.GetAverageEmotion(phase2HappyValues.ToArray());
            EmotionEnergy phase2CalmAverage = Session.GetAverageEmotion(phase2CalmValues.ToArray());
            EmotionEnergy phase2AngryAverage = Session.GetAverageEmotion(phase2AngryValues.ToArray());
            EmotionEnergy phase2SadAverage = Session.GetAverageEmotion(phase2SadValues.ToArray());

            Trace.WriteLine("Phase 1");
            Trace.WriteLine("  Correct: " + _sessions.Average(s => s.Phase1CorrectPercentage) + "%");
            Trace.WriteLine("  Angry: " + _sessions.Sum(s => s.Phase1InvolvingAngryCorrect) + ":" + _sessions.Sum(s => s.Phase1InvolvingAngryIncorrect) + " (" + Math.Round((double)(_sessions.Sum(s => s.Phase1InvolvingAngryCorrect)) / _sessions.Sum(s => s.Phase1InvolvingAngryCorrect + s.Phase1InvolvingAngryIncorrect) * 100f, 2) + "%)");
            Trace.WriteLine("  Happy: " + _sessions.Sum(s => s.Phase1InvolvingHappyCorrect) + ":" + _sessions.Sum(s => s.Phase1InvolvingHappyIncorrect) + " (" + Math.Round((double)(_sessions.Sum(s => s.Phase1InvolvingHappyCorrect)) / _sessions.Sum(s => s.Phase1InvolvingHappyCorrect + s.Phase1InvolvingHappyIncorrect) * 100f, 2) + "%)");
            Trace.WriteLine("  Calm: " + _sessions.Sum(s => s.Phase1InvolvingCalmCorrect) + ":" + _sessions.Sum(s => s.Phase1InvolvingCalmIncorrect) + " (" + Math.Round((double)(_sessions.Sum(s => s.Phase1InvolvingCalmCorrect)) / _sessions.Sum(s => s.Phase1InvolvingCalmCorrect + s.Phase1InvolvingCalmIncorrect) * 100f, 2) + "%)");
            Trace.WriteLine("  Sad: " + _sessions.Sum(s => s.Phase1InvolvingSadCorrect) + ":" + _sessions.Sum(s => s.Phase1InvolvingSadIncorrect) + " (" + Math.Round((double)(_sessions.Sum(s => s.Phase1InvolvingSadCorrect)) / _sessions.Sum(s => s.Phase1InvolvingSadCorrect + s.Phase1InvolvingSadIncorrect) * 100f, 2) + "%)");
            Trace.WriteLine("");

            int consistentPhase2Count = _sessions.Count(s => s.Phase2IsConsistent);
            Trace.WriteLine("Phase 2");
            Trace.WriteLine("   Consistent: " + consistentPhase2Count + " / " + _sessions.Count);
            Trace.WriteLine("   Angry: " + phase2AngryAverage.Quadrant);
            Trace.WriteLine("   Happy: " + phase2HappyAverage.Quadrant);
            Trace.WriteLine("   Calm: " + phase2CalmAverage.Quadrant);
            Trace.WriteLine("   Sad: " + phase2SadAverage.Quadrant);
            Trace.WriteLine("   Angry: " + phase2AngryCorrectQuadrantCount + " selections in correct quadrant of " + phase2AngryTotalQuadrantCount + " (" + Math.Round(((double)phase2AngryCorrectQuadrantCount / (double)phase2AngryTotalQuadrantCount) * 100f, 2) + "%)");
            Trace.WriteLine("   Happy " + phase2HappyCorrectQuadrantCount + " selections in correct quadrant of " + phase2HappyTotalQuadrantCount + " (" + Math.Round(((double)phase2HappyCorrectQuadrantCount / (double)phase2HappyTotalQuadrantCount) * 100f, 2) + "%)");
            Trace.WriteLine("   Calm: " + phase2CalmCorrectQuadrantCount + " selections in correct quadrant of " + phase2CalmTotalQuadrantCount + " (" + Math.Round(((double)phase2CalmCorrectQuadrantCount / (double)phase2CalmTotalQuadrantCount) * 100f, 2) + "%)");
            Trace.WriteLine("   Sad: " + phase2SadCorrectQuadrantCount + " selections in correct quadrant of " + phase2SadTotalQuadrantCount + " (" + Math.Round(((double)phase2SadCorrectQuadrantCount / (double)phase2SadTotalQuadrantCount) * 100f, 2) + "%)");
            Trace.WriteLine("   Angry: " + phase2AngryAverageCorrectQuadrant + " participant averages in correct quadrant of " + consistentPhase2Count + " (" + Math.Round(((double)phase2AngryAverageCorrectQuadrant / (double)consistentPhase2Count) * 100f, 2) + "%)");
            Trace.WriteLine("   Happy: " + phase2HappyAverageCorrectQuadrant + " participant averages in correct quadrant of " + consistentPhase2Count + " (" + Math.Round(((double)phase2HappyAverageCorrectQuadrant / (double)consistentPhase2Count) * 100f, 2) + "%)");
            Trace.WriteLine("   Calm: " + phase2CalmAverageCorrectQuadrant + " participant averages in correct quadrant of " + consistentPhase2Count + " (" + Math.Round(((double)phase2CalmAverageCorrectQuadrant / (double)consistentPhase2Count) * 100f, 2) + "%)");
            Trace.WriteLine("   Sad: " + phase2SadAverageCorrectQuadrant + " participant averages in correct quadrant of " + consistentPhase2Count + " (" + Math.Round(((double)phase2SadAverageCorrectQuadrant / (double)consistentPhase2Count) * 100f, 2) + "%)");
            Trace.WriteLine("");

            Dictionary<string, int> researchWorkHisto = new Dictionary<string, int>();
            foreach (Session s in _sessions)
                foreach (string str in s.DemographicsStudyWork)
                    if (researchWorkHisto.ContainsKey(str))
                        researchWorkHisto[str]++;
                    else
                        researchWorkHisto.Add(str, 1);

            Dictionary<string, int> hvHaP3CountsByWord = new Dictionary<string, int>();
            Dictionary<string, int> hvLaP3CountsByWord = new Dictionary<string, int>();
            Dictionary<string, int> lvHaP3CountsByWord = new Dictionary<string, int>();
            Dictionary<string, int> lvLaP3CountsByWord = new Dictionary<string, int>();
            foreach (string[] wordList in new string[][] { _sessions[0].Phase3HighEnergyHighValencyWords, _sessions[0].Phase3HighEnergyLowValencyWords, _sessions[0].Phase3LowEnergyHighValencyWords, _sessions[0].Phase3LowEnergyLowValencyWords })
                foreach (string str in wordList)
                {
                    hvHaP3CountsByWord.Add(str, 0);
                    lvHaP3CountsByWord.Add(str, 0);
                    hvLaP3CountsByWord.Add(str, 0);
                    lvLaP3CountsByWord.Add(str, 0);
                }
            foreach (Session s in _sessions)
                foreach (string[] wordList in new string[][] { s.Phase3HighEnergyHighValencyWords, s.Phase3HighEnergyLowValencyWords, s.Phase3LowEnergyHighValencyWords, s.Phase3LowEnergyLowValencyWords })
                    foreach (string word in wordList)
                    {
                        EmotionQuadrant q = s.Phase3GetQuadrantForWord(word);
                        Dictionary<string, int> dic;
                        switch (q)
                        {
                            case EmotionQuadrant.HighEnergyLowValency:
                                dic = lvHaP3CountsByWord;
                                break;
                            case EmotionQuadrant.HighEnergyHighValency:
                                dic = hvHaP3CountsByWord;
                                break;
                            case EmotionQuadrant.LowEnergyHighValency:
                                dic = hvLaP3CountsByWord;
                                break;
                            case EmotionQuadrant.LowEnergyLowValency:
                                dic = lvLaP3CountsByWord;
                                break;
                            default:
                                continue;
                        }
                        dic[word]++;
                    }

            Trace.WriteLine("Phase 3");
            foreach (string word in p3CorrectCounts.Keys)
            {
                string mostCommon = "N/A";
                int biggestCount = -1;
                if (p3HeHvCounts[word] > biggestCount)
                {
                    biggestCount = p3HeHvCounts[word];
                    mostCommon = "HeHv";
                }
                if (p3HeLvCounts[word] > biggestCount)
                {
                    biggestCount = p3HeLvCounts[word];
                    mostCommon = "HeLv";
                }
                if (p3LeHvCounts[word] > biggestCount)
                {
                    biggestCount = p3LeHvCounts[word];
                    mostCommon = "LeHv";
                }
                if (p3LeLvCounts[word] > biggestCount)
                {
                    biggestCount = p3LeLvCounts[word];
                    mostCommon = "LeLv";
                }
                Trace.WriteLine("   " + word + ": " + p3CorrectCounts[word] + " correct (" + Math.Round((double)p3CorrectCounts[word] / (double)_sessions.Count * 100f, 2) + "%)    Most Common: " + mostCommon + " with " + biggestCount + " (" + Math.Round((double)biggestCount / (double)_sessions.Count * 100f, 2) + "%)");
            }
            Trace.WriteLine("");

            Trace.WriteLine("Sit With It");
            Trace.WriteLine("   Angry: " + satWithAngryCount + " of " + _sessions.Count + " (" + Math.Round((double)satWithAngryCount / _sessions.Count * 100f, 2) + "%)");
            Trace.WriteLine("   Angry EDA: " + angryEda.Average() + "%    " + angryEda.Count(d => d > 0) + " Up    :    " + angryEda.Count(d => d < 0) + " Down     Up Avg: " + Math.Round(angryEda.Where(d => d > 0f).Average(), 2) + "   Down Avg: " + Math.Round(angryEda.Where(d => d < 0f).Average(), 2));
            Trace.WriteLine("   Calm EDA:  " + calmEda.Average() + "%    " + calmEda.Count(d => d > 0) + " Up    :    " + calmEda.Count(d => d < 0) + " Down   Up Avg:" + Math.Round(calmEda.Where(d => d > 0f).Average(), 2) + "   Down Avg:" + Math.Round(calmEda.Where(d => d < 0f).Average(), 2));
            Trace.WriteLine("");


            Trace.WriteLine("PANAS");
            Trace.WriteLine("  Start +: " + _sessions.Average(s => s.PanasIntroPositive) + "     Calm: " + _sessions.Where(s => !s.SitWithItIsAngry).Average(s => s.PanasIntroPositive) + "     Angry: " + _sessions.Where(s => s.SitWithItIsAngry).Average(s => s.PanasIntroPositive));
            Trace.WriteLine("  End +:   " + _sessions.Average(s => s.PanasOutroPositive) + "     Calm: " + _sessions.Where(s => !s.SitWithItIsAngry).Average(s => s.PanasOutroPositive) + "     Angry: " + _sessions.Where(s => s.SitWithItIsAngry).Average(s => s.PanasOutroPositive));
            Trace.WriteLine("  Start -: " + _sessions.Average(s => s.PanasIntroNegative) + "     Calm: " + _sessions.Where(s => !s.SitWithItIsAngry).Average(s => s.PanasIntroNegative) + "     Angry: " + _sessions.Where(s => s.SitWithItIsAngry).Average(s => s.PanasIntroNegative));
            Trace.WriteLine("  End -:   " + _sessions.Average(s => s.PanasOutroNegative) + "     Calm: " + _sessions.Where(s => !s.SitWithItIsAngry).Average(s => s.PanasOutroNegative) + "     Angry: " + _sessions.Where(s => s.SitWithItIsAngry).Average(s => s.PanasOutroNegative));
            Trace.WriteLine("  Delta +: " + _sessions.Average(s => s.PanasDeltaPositive) + "     Calm: " + _sessions.Where(s => !s.SitWithItIsAngry).Average(s => s.PanasDeltaPositive) + "     Angry: " + _sessions.Where(s => s.SitWithItIsAngry).Average(s => s.PanasDeltaPositive));
            Trace.WriteLine("  Delta -: " + _sessions.Average(s => s.PanasDeltaNegative) + "     Calm: " + _sessions.Where(s => !s.SitWithItIsAngry).Average(s => s.PanasDeltaNegative) + "     Angry: " + _sessions.Where(s => s.SitWithItIsAngry).Average(s => s.PanasDeltaNegative));

            Trace.WriteLine("Preferences");
            Trace.WriteLine("   " + lightCount + " pay attention to light (" + Math.Round((lightCount / (double)_sessions.Count) * 100f, 2) + "%)");
            Trace.WriteLine("   " + vibrationCount + " pay attention to vibrations (" + Math.Round((vibrationCount / (double)_sessions.Count) * 100f, 2) + "%)");
            Trace.WriteLine("Done");

            // PANAS values and differences - correlate to happy/angry cushion as well as overall
            // Extract IPIP Values
            // TODO: Determine Ph3 % correct quandrant
            // Histogram plots and quadrant plots

            File.WriteAllText("out.csv", sb.ToString());

            sb = new StringBuilder();
            sb.AppendLine("SadValency,SadArousal");
            foreach (EmotionEnergy e in _sessions.SelectMany(s => s.Phase2SadReadings))
                sb.AppendLine(e.Valency + "," + e.Arousal);
            File.WriteAllText("phase2sad.csv", sb.ToString());

            sb = new StringBuilder();
            sb.AppendLine("CalmValency,CalmArousal");
            foreach (EmotionEnergy e in _sessions.SelectMany(s => s.Phase2CalmReadings))
                sb.AppendLine(e.Valency + "," + e.Arousal);
            File.WriteAllText("phase2calm.csv", sb.ToString());

            sb = new StringBuilder();
            sb.AppendLine("HappyValency,HappyArousal");
            foreach (EmotionEnergy e in _sessions.SelectMany(s => s.Phase2HappyReadings))
                sb.AppendLine(e.Valency + "," + e.Arousal);
            File.WriteAllText("phase2happy.csv", sb.ToString());

            sb = new StringBuilder();
            sb.AppendLine("AngryValency,AngryArousal");
            foreach (EmotionEnergy e in _sessions.SelectMany(s => s.Phase2AngryReadings))
                sb.AppendLine(e.Valency + "," + e.Arousal);
            File.WriteAllText("phase2angry.csv", sb.ToString());


            Trace.WriteLine("Description", "Count");
            foreach (KeyValuePair<string, int> kvp in researchWorkHisto)
                Trace.WriteLine(kvp.Key + "," + kvp.Value);
            Trace.WriteLine("");

            Trace.WriteLine("High Arousal, High Valency");
            Trace.WriteLine("Word,Count");
            foreach (KeyValuePair<string, int> kvp in hvHaP3CountsByWord)
                Trace.WriteLine(kvp.Key + "," + kvp.Value);
            Trace.WriteLine("");

            Trace.WriteLine("Low Arousal, High Valency");
            Trace.WriteLine("Word,Count");
            foreach (KeyValuePair<string, int> kvp in hvLaP3CountsByWord)
                Trace.WriteLine(kvp.Key + "," + kvp.Value);
            Trace.WriteLine("");

            Trace.WriteLine("Low Arousal, Low Valency");
            Trace.WriteLine("Word,Count");
            foreach (KeyValuePair<string, int> kvp in lvLaP3CountsByWord)
                Trace.WriteLine(kvp.Key + "," + kvp.Value);
            Trace.WriteLine("");

            Trace.WriteLine("High Arousal, Low Valency");
            Trace.WriteLine("Word,Count");
            foreach (KeyValuePair<string, int> kvp in lvHaP3CountsByWord)
                Trace.WriteLine(kvp.Key + "," + kvp.Value);
            Trace.WriteLine("");
        }

        public Point GetPointForPhase3Word(string word)
        {
            return new Point(0, 0);
        }
    }
}

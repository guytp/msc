using Newtonsoft.Json;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
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
                _sessions.Add(JsonConvert.DeserializeObject<Session>(File.ReadAllText(dataFile)));


            int vibrationCount = 0;
            int lightCount = 0;

            int phase3HappyCount = 0;
            int phase3SadCount = 0;
            int phase3CalmCount = 0;
            int phase3AngryCount = 0;

            List<EmotionEnergy> phase2HappyValues = new List<EmotionEnergy>();
            List<EmotionEnergy> phase2SadValues = new List<EmotionEnergy>();
            List<EmotionEnergy> phase2CalmValues = new List<EmotionEnergy>();
            List<EmotionEnergy> phase2AngryValues = new List<EmotionEnergy>();

            foreach (Session session in _sessions)
            {
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

                Trace.WriteLine("A: " + session.Phase2AngryAverage + "   H: " + session.Phase2HappyAverage + "    C: " + session.Phase2CalmAverage + "   S: " + session.Phase2SadAverage);
                phase2AngryValues.AddRange(session.Phase2AngryReadings);
                phase2HappyValues.AddRange(session.Phase2HappyReadings);
                phase2CalmValues.AddRange(session.Phase2CalmReadings);
                phase2SadValues.AddRange(session.Phase2SadReadings);
            }

            EmotionEnergy phase2HappyAverage = Session.GetAverageEmotion(phase2HappyValues.ToArray());
            EmotionEnergy phase2CalmAverage = Session.GetAverageEmotion(phase2CalmValues.ToArray());
            EmotionEnergy phase2AngryAverage = Session.GetAverageEmotion(phase2AngryValues.ToArray());
            EmotionEnergy phase2SadAverage = Session.GetAverageEmotion(phase2SadValues.ToArray());
            Trace.WriteLine("A: " + phase2AngryAverage + "   H: " + phase2HappyAverage + "    C: " + phase2CalmAverage + "   S: " + phase2SadAverage);
            Trace.WriteLine("Done");

            // TODO: Determine Ph2 % right quadrant
            // TODO: Determine Ph3 % right quandrant
            // ToDo: Quick Ph1 analysis

        }

        public Point GetPointForPhase3Word(string word)
        {
            return new Point(0, 0);
        }
    }
}

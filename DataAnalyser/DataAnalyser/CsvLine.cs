using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DataAnalyser
{
    public class CsvLine
    {
        private readonly static string[] Headers = new[] { "SessionId", "Time", "DemographicsAge", "DemographicsGender", "DemographicsEthnicity", "DemographicsEducation", "AngryOrCalm", "EdaSitWithStart", "EdaSitWithEnd", "EdaSitWithDelta", "VibrationLightOrNeither", "PanasIntroScorePositive", "PanasIntroScoreNegative", "PanasOutroScorePositive", "PanasOutroScoreNegative", "PanasScoreDeltaPositive", "PanasScoreDeltaNegative", "IpipScore", "Phase1CorrectPercentage", "Phase1CorrectPercentageInvolvingAngry", "Phase1CorrectPercentageInvolvingCalm", "Phase1CorrectPercentageInvolvingSad", "Phase1CorrectPercentageInvolvingHappy", "Phase2SadAverageValency", "Phase2SadAverageArousal", "Phase2SadConsistencyPercentage", "Phase2CalmAverageValency", "Phase2CalmAverageArousal", "Phase2CalmConsistencyPercentage", "Phase2HappyAverageValency", "Phase2HappyAverageArousal", "Phase2HappyConsistencyPercentage", "Phase2AngryAverageValency", "Phase2AngryAverageArousal", "Phase2AngryConsistencyPercentage", "Phase3Excited", "Phase3Happy", "Phase3Surprised", "Phase3Delighted", "Phase3Cheerful", "Phase3Relaxed", "Phase3Calm", "Phase3Serene", "Phase3Content", "Phase3Pleased", "Phase3Sad", "Phase3Depressed", "Phase3Gloomy", "Phase3Bored", "Phase3Tired", "Phase3Afraid", "Phase3Angry", "Phase3Annoyed", "Phase3Frustrated", "Phase3Terrified" };

        public static CsvLine Header { get; private set; }

        static CsvLine()
        {
            string str = "";
            foreach (string s in Headers)
                str += (str == "" ? "" : ",") + s;
            Header = new CsvLine(str);
        }

        private string _value;

        private CsvLine(string text)
        {
            _value = text;
        }

        public override string ToString()
        {
            return _value;
        }

        public CsvLine(Session session)
        {
            string str = "";
            foreach (string component in Headers)
            {
                string thisValue ;
                switch (component)
                {
                    case "DemographicsAge":
                        thisValue = session.DemographicsAge;
                        break;
                    case "DemographicsGender":
                        thisValue = session.DemographicsGender;
                        break;
                    case "DemographicsEthnicity":
                        thisValue = session.DemographicsEthnicity;
                        break;
                    case "DemographicsEducation":
                        thisValue = session.DemographicsEducation;
                        break;
                    case "SessionId":
                        thisValue = session.SessionId.ToString();
                        break;
                    case "EdaSitWithStart":
                        thisValue = session.SitWithItStartEda.ToString();
                        break;
                    case "EdaSitWithEnd":
                        thisValue = session.SitWithItEndEda.ToString();
                        break;
                    case "EdaSitWithDelta":
                        thisValue = session.SitWithItEdaDeltaPercent.ToString();
                        break;
                    case "Time":
                        thisValue = session.StartTime.ToString();
                        break;
                    case "AngryOrCalm":
                        thisValue = session.SitWithItIsAngry ? "Angry" : "Calm";
                        break;
                    case "VibrationLightOrNeither":
                        thisValue = session.GetData("Phase3.VibrationOrLight");
                        break;
                    case "PanasIntroScorePositive":
                        thisValue = session.PanasIntroPositive.ToString();
                        break;
                    case "PanasIntroScoreNegative":
                        thisValue = session.PanasIntroNegative.ToString();
                        break;
                    case "PanasOutroScorePositive":
                        thisValue = session.PanasOutroPositive.ToString();
                        break;
                    case "PanasOutroScoreNegative":
                        thisValue = session.PanasOutroNegative.ToString();
                        break;
                    case "PanasScoreDeltaPositive":
                        thisValue = session.PanasDeltaPositive.ToString();
                        break;
                    case "PanasScoreDeltaNegative":
                        thisValue = session.PanasDeltaNegative.ToString();
                        break;
                    case "IpipScore":
                        thisValue = session.Ipip.ToString();
                        break;
                    case "Phase1CorrectPercentage":
                        thisValue = session.Phase1CorrectPercentage.ToString();
                        break;
                    case "Phase1CorrectPercentageInvolvingAngry":
                        thisValue = Math.Round((double)session.Phase1InvolvingAngryCorrect / (double)(session.Phase1InvolvingAngryCorrect + session.Phase1InvolvingAngryIncorrect) * 100f, 2).ToString();
                        break;
                    case "Phase1CorrectPercentageInvolvingCalm":
                        thisValue = Math.Round((double)session.Phase1InvolvingCalmCorrect / (double)(session.Phase1InvolvingCalmCorrect + session.Phase1InvolvingCalmIncorrect) * 100f, 2).ToString();
                        break;
                    case "Phase1CorrectPercentageInvolvingSad":
                        thisValue = Math.Round((double)session.Phase1InvolvingSadCorrect / (double)(session.Phase1InvolvingSadCorrect + session.Phase1InvolvingSadIncorrect) * 100f, 2).ToString();
                        break;
                    case "Phase1CorrectPercentageInvolvingHappy":
                        thisValue = Math.Round((double)session.Phase1InvolvingHappyCorrect / (double)(session.Phase1InvolvingHappyCorrect + session.Phase1InvolvingHappyIncorrect) * 100f, 2).ToString();
                        break;
                    case "Phase2SadAverageValency":
                        thisValue = session.Phase2SadAverage.Valency.ToString();
                        break;
                    case "Phase2SadAverageArousal":
                        thisValue = session.Phase2SadAverage.Arousal.ToString();
                        break;
                    case "Phase2SadConsistencyPercentage":
                        thisValue = session.Phase2SadConsistency.ToString();
                        break;
                    case "Phase2CalmAverageValency":
                        thisValue = session.Phase2CalmAverage.Valency.ToString();
                        break;
                    case "Phase2CalmAverageArousal":
                        thisValue = session.Phase2CalmAverage.Arousal.ToString();
                        break;
                    case "Phase2CalmConsistencyPercentage":
                        thisValue = session.Phase2CalmConsistency.ToString();
                        break;
                    case "Phase2HappyAverageValency":
                        thisValue = session.Phase2HappyAverage.Valency.ToString();
                        break;
                    case "Phase2HappyAverageArousal":
                        thisValue = session.Phase2HappyAverage.Arousal.ToString();
                        break;
                    case "Phase2HappyConsistencyPercentage":
                        thisValue = session.Phase2HappyConsistency.ToString();
                        break;
                    case "Phase2AngryAverageValency":
                        thisValue = session.Phase2AngryAverage.Valency.ToString();
                        break;
                    case "Phase2AngryAverageArousal":
                        thisValue = session.Phase2AngryAverage.Arousal.ToString();
                        break;
                    case "Phase2AngryConsistencyPercentage":
                        thisValue = session.Phase2AngryConsistency.ToString();
                        break;
                    default:
                        if (component.StartsWith("Phase3"))
                        {
                            thisValue = session.Phase3GetQuadrantForWord(component.Substring(6)).ToString();
                            break;
                        }
                        else
                            throw new Exception("No parser");
                }
                str += (str == "" ? "" : ",") + thisValue;
            }
            _value = str;
        }
    }
}
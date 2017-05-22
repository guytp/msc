using HtmlAgilityPack;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;

namespace AmazonScraper
{
    class Program
    {
        static void Main(string[] args)
        {
            if (args.Length < 5)
            {
                Console.WriteLine("Usage: AmazonScraper <output JSON file> <output text file> <output positive text file> <output negative text file> <asin 1> [asin 2] ... [asin n]");
                return;
            }

            List<Review> reviews = new List<Review>();
            StringBuilder allBodyWords = new StringBuilder();
            StringBuilder positiveBodyWords = new StringBuilder();
            StringBuilder negativeBodyWords = new StringBuilder();
            for (int i = 4; i < args.Length; i++)
            {
                string asin = args[i];
                using (WebClient client = new WebClient())
                {
                    try
                    {
                        string urlBase = "https://www.amazon.co.uk/product-reviews/" + asin + "/?ie=UTF8&reviewerType=all_reviews&showViewpoints=1&sortBy=recent&pageNumber=";
                        int pageNumber = 1;
                        while (true)
                        {
                            // Get the HTML and parse it into an XML document
                            Console.WriteLine("Downloading " + asin + " page " + pageNumber);
                            string html = client.DownloadString(urlBase + pageNumber);
                            HtmlDocument doc = new HtmlDocument();
                            doc.LoadHtml(html);

                            // Stop if we have reached the end
                            if (doc.DocumentNode.InnerHtml.Contains("Sorry, no reviews match your current selections."))
                                break;

                            // Get a handle to main list of review nodes
                            HtmlNodeCollection reviewNodes = doc.DocumentNode.SelectNodes("//div[@id='cm_cr-review_list']/div[@data-hook='review']");
                            if (reviewNodes == null || reviewNodes.Count == 0)
                                break;

                            // Parse out each review
                            foreach (HtmlNode reviewNode in reviewNodes)
                            {
                                string reviewId = reviewNode.Id;
                                decimal rating = decimal.Parse(reviewNode.SelectSingleNode("//div[@id='" + reviewId + "']//i[@data-hook='review-star-rating']").InnerText.Split(new char[] { ' ' }, 2)[0]);
                                string title = reviewNode.SelectSingleNode("//div[@id='" + reviewId + "']//a[@data-hook='review-title']").InnerText;
                                string author = reviewNode.SelectSingleNode("//div[@id='" + reviewId + "']//a[@data-hook='review-author']").InnerText;
                                string review = reviewNode.SelectSingleNode("//div[@id='" + reviewId + "']//span[@data-hook='review-body']").InnerText;
                                if (rating <= 2)
                                {
                                    if (negativeBodyWords.Length > 0)
                                        negativeBodyWords.AppendLine();
                                    negativeBodyWords.Append(review);
                                }
                                else if (rating >= 4)
                                {
                                    if (positiveBodyWords.Length > 0)
                                        positiveBodyWords.AppendLine();
                                    positiveBodyWords.Append(review);
                                }
                                if (allBodyWords.Length > 0)
                                    allBodyWords.AppendLine();
                                allBodyWords.Append(review);
                                reviews.Add(new Review(asin, reviewId, title, rating, author, review));
                            }

                            // Increment for next page
                            pageNumber++;
                        }
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("Error downloading data\r\n" + ex);
                        return;
                    }
                }
            }

            // Now write the data out
            File.WriteAllText(args[0], JsonConvert.SerializeObject(reviews));
            File.WriteAllText(args[1], allBodyWords.ToString());
            File.WriteAllText(args[2], positiveBodyWords.ToString());
            File.WriteAllText(args[3], negativeBodyWords.ToString());

            // Confirm
            Console.Write("Reviews downloaded (" + reviews.Count + " for " + (args.Length - 4) + " ASIN" + (args.Length > 5 ? "s" : "") + ").\r\n\r\nPress any key to exit...");
            Console.ReadKey();
        }
    }
}
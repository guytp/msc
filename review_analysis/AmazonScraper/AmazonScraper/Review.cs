namespace AmazonScraper
{
    class Review
    {
        public string Id { get; private set; }
        public decimal Rating { get; private set; }

        public string Title { get; private set; }

        public string Author { get; private set; }

        public string Content { get; private set; }

        public string Asin { get; private set; }

        public Review (string asin, string reviewId, string title, decimal rating, string author, string content)
        {
            Asin = asin;
            Id = reviewId;
            Title = title;
            Rating = rating;
            Author = author;
            Content = content;
        }
    }
}

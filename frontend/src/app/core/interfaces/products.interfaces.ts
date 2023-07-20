// Generated by https://quicktype.io

export interface FormProducts {
  page: number;
  country?: string;
  category?: string;
}

// Generated by https://quicktype.io

export interface ProductsResponse {
  id: string;
  name: string;
  description: string;
  stock: number;
  image: string;
  price: number;
  weight: number;
  country: string;
  minStock: number;
  state: boolean;
  category: Category;
}

export interface Category {
  id: string;
  name: string;
  description: string;
  number: string;
  state: boolean;
}

// Generated by https://quicktype.io

export interface ProductById {
  id:          string;
  name:        string;
  description: string;
  stock:       number;
  image:       string;
  price:       number;
  weight:      number;
  country:     string;
  minStock:    number;
  state:       string;
  category:    Category;
}

export interface Category {
  id:          string;
  name:        string;
  description: string;
  number:      string;
  state:       boolean;
}

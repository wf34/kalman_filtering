import csv

import numpy as np
import matplotlib.pyplot as plt

def load_time_lat_lon(filename):
    dataset = []
    with open(filename, 'r') as the_file:
        reader = csv.reader(the_file)
        next(reader, None)
        for row in reader:
            row = list(map(lambda x : float(x), row))
            dataset.append(row)
    return np.array(dataset)


def visualize(path, dataset):
    plt.scatter(dataset[:, 1], dataset[:, 2], c = dataset[:, 0])
    plt.savefig(path)
    plt.close()


def main():
    for x in ['1.csv', '2.csv']:
        dataset = load_time_lat_lon(x)
        visualize(x[:-4] + '.png', dataset)
    

if '__main__' == __name__:
    main()

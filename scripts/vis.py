import csv
import sys

import numpy as np
import matplotlib.pyplot as plt

def load_time_lat_lon(filename):
    dataset = []
    with open(filename, 'r') as the_file:
        reader = csv.reader(the_file)
        _ = next(reader, None)
        for row in reader:
            row = list(map(lambda x : float(x), row))
            row[1], row[2] = row[2], row[1]
            dataset.append(row)
    return np.array(dataset)


def visualize(ax, dataset, vis_color, point_style = '-o', thickness = 1):
    if dataset and len(dataset):
        dataset = np.array(dataset)
        ax.plot(dataset[:, 1], dataset[:, 2],
                point_style,  color = vis_color,
                linewidth = thickness)
        

def do_graph(path, first_dataset, second_dataset = None):
    f = plt.figure()
    ax = f.add_subplot(111)
    visualize(ax, first_dataset, 'red', '-o', 3)
    visualize(ax, second_dataset, 'blue', '-v', 1)
    f.savefig(path)

def main():
    csv_filepath = sys.argv[1]
    dataset = load_time_lat_lon(csv_filepath)
    do_graph(csv_filepath[:-4] + '.png', dataset)
    

if '__main__' == __name__:
    main()
